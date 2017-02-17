package se.kth.id2203.broadcast.beb;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.kth.id2203.bootstrapping.PutKey;
import se.kth.id2203.bootstrapping.PutKeyAck;
import se.kth.id2203.networking.Message;
import se.kth.id2203.networking.NetAddress;
import se.sics.kompics.*;
import se.sics.kompics.network.Network;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Created by Nick on 2/16/2017.
 */
public class BasicBroadcast extends ComponentDefinition {

    final static Logger LOG = LoggerFactory.getLogger(BasicBroadcast.class);
    //******* Ports ******
    protected final Positive<Network> net = requires(Network.class);
    protected final Negative<BestEffortBroadcast> beb = provides(BestEffortBroadcast.class);
    //******* Fields ******
    private NetAddress self = config().getValue("id2203.project.address", NetAddress.class);
    private NetAddress server = config().getValue("id2203.project.bootstrap-address", NetAddress.class);
    private Map<NetAddress, String> acks;


    protected void resetAcks(){
        //Reset the acks
        acks = new HashMap<>();
    }

    private boolean messageIsFromMaster(Message m){
        return m.getSource().getPort() == server.getPort() && m.getSource().getIp().equals(server.getIp());
    }

    protected void determineDelivery(PutKey putKey, Message message){

        /*
        //Ignore messages from master
        if(messageIsFromMaster(message)){
            return;
        }
        */


        Set<NetAddress> topology = putKey.topology;

        //Lazy instantiation
        if(acks == null){
            resetAcks();
        }

        if(!acks.containsKey(self)){
            System.out.println("Start broadcast");

            //This is reliable broadcast, not used in BEB?
            //trigger(new BEB_Broadcast(putKey, topology), beb);
            trigger(new Message());

            acks.put(self, putKey.key);
        }

        acks.put(message.getSource(), putKey.key);

        boolean allDone = true;
        for (NetAddress adr : topology){
            if(!acks.containsKey(adr)){
                allDone = false;
                break;
            }
        }

        if(allDone){
            //Deliver
            System.out.println("deliver: " + self);
            //trigger(new Message(self, server, new PutKeyAck(putKey.key, putKey.client)), net);
            trigger(new BEB_Deliver(self, putKey), beb);
            resetAcks();
        }
    }

    //******* Handlers ******
    protected final Handler<BEB_Broadcast> broadcastHandler = new Handler<BEB_Broadcast>() {
        @Override
        public void handle(BEB_Broadcast beb_broadcast) {
            System.out.println("BEB start in BasicBroadcast");



            resetAcks();

            //Use the current topology on a new broadcast
            Set<NetAddress> topology = beb_broadcast.topology;
            System.out.println(beb_broadcast);

            //Trigger a broadcast to all in topology
            for (NetAddress adr : topology) {
                System.out.println("BEB trigger to " + adr);
                PutKey putKey = (PutKey) beb_broadcast.payload;

                //Add the topology, this might become a vector clock later
                putKey.setTopology(topology);


                trigger(new Message(self, adr, putKey), net);
            }

        }
    };
    protected final ClassMatchedHandler<PutKey, Message> putKeyHandler = new ClassMatchedHandler<PutKey, Message>() {
        @Override
        public void handle(PutKey putKey, Message message) {
            determineDelivery(putKey, message);
        }
    };


    {
        subscribe(putKeyHandler, net);
        subscribe(broadcastHandler, beb);
    }
}
