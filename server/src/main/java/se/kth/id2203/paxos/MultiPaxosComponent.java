package se.kth.id2203.paxos;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.kth.id2203.broadcast.beb.TopologyResponse;
import se.kth.id2203.broadcast.perfect_link.PL_Deliver;
import se.kth.id2203.broadcast.perfect_link.PL_Send;
import se.kth.id2203.broadcast.perfect_link.PerfectLink;
import se.kth.id2203.kvstore.Operation;
import se.kth.id2203.networking.Message;
import se.kth.id2203.networking.NetAddress;
import se.sics.kompics.*;
import se.sics.kompics.network.Network;

import java.util.*;

/**
 * Created by Nick on 2/23/2017.
 */
public class MultiPaxosComponent extends ComponentDefinition {
    final static Logger LOG = LoggerFactory.getLogger(MultiPaxosComponent.class);
    //******* Ports ******
    protected final Positive<PerfectLink> fpl = requires(PerfectLink.class);
    protected final Positive<Network> net = requires(Network.class);
    protected final Negative<MultiPaxos> asc = provides(MultiPaxos.class);
    //******* Fields ******
    private NetAddress self = config().getValue("id2203.project.address", NetAddress.class);
    private Set<NetAddress> topology = new HashSet<>();

    private int t;
    private int prepts;
    private int ats;
    private int al;
    private int pts;
    private int pl;

    private List<Operation> av;
    private List<Operation> pv;
    private List<Operation> proposedValues;

    private boolean updateTopology = false;
    private Set<NetAddress> newTopology = new HashSet<>();

    private Map<NetAddress, ReadlistItem> readlist;

    private Map<NetAddress, Integer> accepted;
    private Map<NetAddress, Integer> decided;

    private void init(){
        t = 0;
        prepts = 0;

        //Acceptor
        ats = 0;
        av = new ArrayList<>();
        al = 0;


        //Proposer
        pts = 0;
        pv = new ArrayList<>();
        pl = 0;

        proposedValues = new ArrayList<>();

        readlist = new HashMap<>();

        accepted = new HashMap<>();
        decided = new HashMap<>();
    }


    private int getN(){
       return topology.size();
    }

    private int rank(NetAddress adr){
        return adr.hashCode();
    }

    private List<Operation> prefix(List<Operation> whole, int length){
        return whole.subList(0, length);
    }

    private List<Operation> suffix(List<Operation> whole, int length){
        return whole.subList(length, whole.size());
    }

    private int getMinorityCount(){
        return (int)Math.floor(getN() / 2.0 );
    }

    private void propose(Propose propose){
        t++;

        if (pts == 0){

            if(updateTopology){
                topology = newTopology;
                updateTopology = false;
            }

            pts = t * getN() + rank((self));
            pv = prefix(av, al);
            pl = 0;
            proposedValues = new ArrayList<>();
            proposedValues.add(propose.value);

            readlist = new HashMap<>();
            accepted = new HashMap<>();
            decided = new HashMap<>();

            for(NetAddress p : topology){
                trigger(new PL_Send(self, p, new Prepare(pts, al, t)), fpl);
            }

        }else if(readlist.size() <= getMinorityCount()){
            proposedValues.add(propose.value);
        }else if(!pv.contains(propose.value)){
            pv.add(propose.value);

            for(NetAddress p : topology){
                if(readlist.containsKey(p)){
                    List<Operation> opts = new ArrayList<>();
                    opts.add(propose.value);
                    trigger(new PL_Send(self, p, new Accept(pts, opts, pv.size() -1, t)), fpl);
                }

            }
        }
    }

    private void prepare(Prepare prepare, NetAddress q){
        t = Math.max(t, prepare.t) + 1;
        if(prepare.pts < prepts){
            trigger(new PL_Send(self, q, new Nack(prepare.pts, t)), fpl);
        }else{
            prepts = prepare.pts;
            trigger(new PL_Send(self, q, new PrepareAck(prepare.pts, ats, al, t, suffix(av, prepare.al))), fpl);
        }
    }

    private void nack(Nack nack){
        t = Math.max(t, nack.t) + 1;

        if(nack.ts == pts){
            pts = 0;
            trigger(new Abort("whatever"), asc);
        }
    }


    private void prepareAck(PrepareAck prepareAck, NetAddress q){
        t = Math.max(t, prepareAck.t) + 1;
        if(prepareAck.ts == pts){
            readlist.put(q, new ReadlistItem(prepareAck.ats, prepareAck.vsuf));
            decided.put(q, prepareAck.al);

            if(readlist.size() == getMinorityCount() + 1){
                ReadlistItem readlistItem = new ReadlistItem(0, new ArrayList<Operation>());

                for (ReadlistItem rli : readlist.values()){
                    if(readlistItem.ts < rli.ts || (readlistItem.ts == rli.ts && readlistItem.vsuf.size() < rli.vsuf.size())){
                        readlistItem = new ReadlistItem(rli.ts, rli.vsuf);
                    }
                }


               // pv.addAll(readlistItem.vsuf);

                for(Operation op : readlistItem.vsuf){
                    pv.add(op);
                }

                for(Operation v : proposedValues){
                    if(!pv.contains(v)){
                        pv.add(v);
                    }
                }

                for(NetAddress p : topology){
                    if(readlist.containsKey(p)){
                        int _l = decided.get(p);
                        trigger(new PL_Send(self, p, new Accept(pts, suffix(pv, _l), _l, t)), fpl);
                    }
                }

            }else if(readlist.size() > getMinorityCount() + 1){
                trigger(new PL_Send(self, q, new Accept(pts, suffix(pv, prepareAck.al), prepareAck.al, t)), fpl);
                if(pl != 0){
                    trigger(new PL_Send(self, q, new Decide(pts, pl, t)), fpl);
                }
            }
        }
    }


    private void accept(Accept accept, NetAddress q){
        t = Math.max(t, accept.t) +1;
        if(accept.pts != prepts){
            trigger(new PL_Send(self, q, new Nack(accept.pts, t)), fpl);
        }else{
            ats = accept.pts;
            if(accept.offs < av.size()){
                av = prefix(av, accept.offs);
            }

            av.addAll(accept.vsuff);
            trigger(new PL_Send(self, q, new AcceptAck(accept.pts, av.size(), t)), fpl);
        }
    }

    private void acceptAck(AcceptAck acceptAck, NetAddress q){

        t = Math.max(t, acceptAck.t) + 1;

        if(acceptAck.pts == pts){
            accepted.put(q, acceptAck.l);

            int acceptedCount = 0;

            for(NetAddress p : topology){

                if(accepted.get(p) >= acceptAck.l){
                    acceptedCount ++;
                }
            }

            if(pl < acceptAck.l && acceptedCount > getMinorityCount()){
                pl = acceptAck.l;

                for (NetAddress p : topology){
                    if(readlist.containsKey(p)){
                        trigger(new PL_Send(self, p, new Decide(pts, pl, t)), fpl);
                    }
                }
            }
        }
    }

    private void decide(Decide decide){
        t = Math.max(t, decide.t) + 1;
        if(decide.pts == prepts){
            while (al < decide.pl){
                trigger(new FinalDecide(av.get(al)), asc);
                al++;
            }
        }
    }


    protected final Handler<Start> startHander = new Handler<Start>() {
        @Override
        public void handle(Start start) {
            System.out.println("PAXOS, init");
            init();
        }
    };

    protected final ClassMatchedHandler<Decide, PL_Deliver> decideHandler = new ClassMatchedHandler<Decide, PL_Deliver>() {
        @Override
        public void handle(Decide decide, PL_Deliver pl_deliver) {
            System.out.println("PAXOS, decideHandler");
            decide(decide);
        }
    };

    protected final ClassMatchedHandler<AcceptAck, PL_Deliver> acceptAck = new ClassMatchedHandler<AcceptAck, PL_Deliver>() {
        @Override
        public void handle(AcceptAck acceptAck, PL_Deliver pl_deliver) {
            System.out.println("PAXOS, acceptAck");


            acceptAck(acceptAck, pl_deliver.src);
        }
    };


    protected final ClassMatchedHandler<Accept, PL_Deliver> acceptHander = new ClassMatchedHandler<Accept, PL_Deliver>() {
        @Override
        public void handle(Accept accept, PL_Deliver pl_deliver) {
            System.out.println("PAXOS, acceptHander");
            accept(accept, pl_deliver.src);
        }
    };

    protected final ClassMatchedHandler<PrepareAck, PL_Deliver> prepareACkHandler = new ClassMatchedHandler<PrepareAck, PL_Deliver>() {
        @Override
        public void handle(PrepareAck prepareAck, PL_Deliver pl_deliver) {
            System.out.println("PAXOS, prepareACkHandler");
            prepareAck(prepareAck, pl_deliver.src);
        }
    };

    protected final ClassMatchedHandler<Nack, PL_Deliver> nackHandler = new ClassMatchedHandler<Nack, PL_Deliver>() {
        @Override
        public void handle(Nack nack, PL_Deliver pl_deliver) {
            System.out.println("PAXOS, nackHandler");
            nack(nack);
        }
    };

    protected final ClassMatchedHandler<Prepare, PL_Deliver> prepareHandler = new ClassMatchedHandler<Prepare, PL_Deliver>() {
        @Override
        public void handle(Prepare prepare, PL_Deliver pl_deliver) {
            System.out.println("PAXOS, prepareHandler");
            prepare(prepare, pl_deliver.src);
        }
    };


    protected final ClassMatchedHandler<TopologyResponse, Message> topologyResponseMessageClassMatchedHandler = new ClassMatchedHandler<TopologyResponse, Message>() {
        @Override
        public void handle(TopologyResponse topologyResponse, Message message) {
            newTopology = topologyResponse.topology;
            updateTopology = true;

            System.out.println("----- Topology received at MultiPaxos ---");
            System.out.println(newTopology);
            System.out.println("-----------------");

        }
    };

    protected final Handler<Propose> proposeHandler = new Handler<Propose>() {
        @Override
        public void handle(Propose propose) {
            //System.out.println("Multipaxos at " + self + " got " + propose);
            propose(propose);
            //trigger(new Decide("someval"), asc);
        }
    };

    {
        subscribe(decideHandler, fpl);
        subscribe(acceptAck, fpl);
        subscribe(acceptHander, fpl);
        subscribe(prepareACkHandler, fpl);
        subscribe(nackHandler, fpl);
        subscribe(prepareHandler, fpl);
        subscribe(startHander, control);
        subscribe(topologyResponseMessageClassMatchedHandler, net);
        subscribe(proposeHandler, asc);
    }
}
