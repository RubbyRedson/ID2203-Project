package se.kth.id2203.paxos;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.kth.id2203.broadcast.beb.TopologyResponse;
import se.kth.id2203.broadcast.epfd.EpfdComponent;
import se.kth.id2203.broadcast.epfd.EventuallyPerfectFailureDetector;
import se.kth.id2203.broadcast.epfd.Timeout;
import se.kth.id2203.broadcast.perfect_link.PL_Deliver;
import se.kth.id2203.broadcast.perfect_link.PerfectLink;
import se.kth.id2203.networking.Message;
import se.kth.id2203.networking.NetAddress;
import se.sics.kompics.*;
import se.sics.kompics.network.Network;
import se.sics.kompics.timer.ScheduleTimeout;
import se.sics.kompics.timer.Timer;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by Nick on 2/23/2017.
 */
public class MultiPaxosComponent extends ComponentDefinition {
    final static Logger LOG = LoggerFactory.getLogger(MultiPaxosComponent.class);
    //******* Ports ******
    protected final Positive<Timer> timer = requires(Timer.class);
    protected final Positive<PerfectLink> fpl = requires(PerfectLink.class);
    protected final Positive<Network> net = requires(Network.class);
    protected final Negative<MultiPaxos> asc = provides(MultiPaxos.class);
    //******* Fields ******
    private NetAddress self = config().getValue("id2203.project.address", NetAddress.class);
    private Set<NetAddress> topology = new HashSet<>();

    private int timestamp = 0;
    private int preparedTimestamp = 0;

    private boolean updateTopology = false;
    private Set<NetAddress> newTopology = new HashSet<>();


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
            System.out.println("Multipaxos at " + self + " got " + propose);
            trigger(new Decide("someval"), asc);
//            ScheduleTimeout spt = new ScheduleTimeout(200);
//            spt.setTimeoutEvent(new Timeout(spt));
//            trigger(spt, timer);
        }
    };
    protected final Handler<Timeout> timeoutHandler = new Handler<Timeout>() {
        @Override
        public void handle(Timeout e) {
            System.out.println("Fire decide");
            trigger(new Decide("someval"), asc);
        }
    };

    {
        subscribe(timeoutHandler, timer);
        subscribe(topologyResponseMessageClassMatchedHandler, net);
        subscribe(proposeHandler, asc);
    }
}
