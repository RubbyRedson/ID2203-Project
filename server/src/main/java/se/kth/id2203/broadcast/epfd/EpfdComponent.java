package se.kth.id2203.broadcast.epfd;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.kth.id2203.broadcast.beb.TopologyResponse;
import se.kth.id2203.broadcast.perfect_link.PL_Deliver;
import se.kth.id2203.broadcast.perfect_link.PL_Send;
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
 * Created by Nick on 2/17/2017.
 */
public class EpfdComponent extends ComponentDefinition {
    final static Logger LOG = LoggerFactory.getLogger(EpfdComponent.class);
    //******* Ports ******
    protected final Positive<Timer> timer = requires(Timer.class);
    protected final Positive<PerfectLink> pLink = requires(PerfectLink.class);
    protected final Positive<Network> net = requires(Network.class);
    protected final Negative<EventuallyPerfectFailureDetector> epfd = provides(EventuallyPerfectFailureDetector.class);
    //******* Fields ******
    private NetAddress self = config().getValue("id2203.project.address", NetAddress.class);
    private NetAddress server = config().getValue("id2203.project.bootstrap-address", NetAddress.class);
    private static int DELAY = 1000;
    private int delay = 1000;
    private Set<NetAddress> topology = new HashSet<>();

    private Set<NetAddress> alive = new HashSet<>();
    private Set<NetAddress> suspected = new HashSet<>();
    private int seqnum = 0;

    private boolean updateTopology = false;
    private Set<NetAddress> newTopology = new HashSet<>();

    protected final Handler<Start> startHandler = new Handler<Start>() {
        @Override
        public void handle(Start e) {
            System.out.println("Start triggered at " + self);

            ScheduleTimeout spt = new ScheduleTimeout(delay);
            spt.setTimeoutEvent(new Timeout(spt));
            trigger(spt, timer);
        }
    };
    protected final Handler<Timeout> timeoutHandler = new Handler<Timeout>() {
        @Override
        public void handle(Timeout e) {
            System.out.println("Timer triggered at " + self);
            if (updateTopology) {
                topology = newTopology;
                alive = copySet(topology);
                suspected.clear();
                updateTopology = false;
            }

            if (hasIntersection(alive, suspected)) {
                delay = delay + DELAY;
            }
            seqnum += 1;
            for (NetAddress p : topology) {
                if (!alive.contains(p) && !suspected.contains(p)) {
                    trigger(new Suspect(p), epfd);
                } else if (alive.contains(p) && suspected.contains(p)) {
                    trigger(new Restore(p), epfd);
                }
                trigger(new PL_Send(self, p, new HeartbeatRequest(seqnum)), pLink);
            }
            alive.clear();

            ScheduleTimeout spt = new ScheduleTimeout(delay);
            spt.setTimeoutEvent(new Timeout(spt));
            trigger(spt, timer);
        }
    };

    protected final ClassMatchedHandler<HeartbeatRequest, PL_Deliver> plDeliverRequestHandler = new ClassMatchedHandler<HeartbeatRequest, PL_Deliver>() {
        @Override
        public void handle(HeartbeatRequest heartbeatRequest, PL_Deliver pl_deliver) {
            System.out.println("HeartbeatRequest received at " + self);
            trigger(new PL_Send(self, pl_deliver.src, new HeartbeatReply(seqnum)), pLink);
        }
    };
    protected final ClassMatchedHandler<HeartbeatReply, PL_Deliver> plDeliverResponseHandler = new ClassMatchedHandler<HeartbeatReply, PL_Deliver>() {
        @Override
        public void handle(HeartbeatReply heartbeatReply, PL_Deliver pl_deliver) {
            System.out.println("HeartbeatReply received at " + self);
            if (seqnum == heartbeatReply.seq || suspected.contains(pl_deliver.src)) {
                alive.add(pl_deliver.src);
            }
        }
    };
    protected final ClassMatchedHandler<TopologyResponse, Message> topologyResponseMessageClassMatchedHandler = new ClassMatchedHandler<TopologyResponse, Message>() {
        @Override
        public void handle(TopologyResponse topologyResponse, Message message) {
            newTopology = topologyResponse.topology;
            updateTopology = true;

            System.out.println("----- Topology received at EPFD ---");
            System.out.println(newTopology);
            System.out.println("-----------------");
        }
    };
    private boolean hasIntersection(Set a1, Set a2) {
        if (a1.size() == 0 || a2.size() == 0)
            return false;

        if (a1.size() > a2.size()) {
            for (Object o : a1) {
                if (a2.contains(o))
                    return true;
            }
        } else {
            for (Object o : a2) {
                if (a1.contains(o))
                    return true;
            }
        }
        return false;
    }

    private Set<NetAddress> copySet(Set<NetAddress> toCopy) {
        Set<NetAddress> result = new HashSet<>();
        for (NetAddress address : toCopy)
            result.add(address);
        return result;
    }

    {
        subscribe(startHandler, control);
        subscribe(timeoutHandler, timer);
        subscribe(plDeliverRequestHandler, pLink);
        subscribe(plDeliverResponseHandler, pLink);
        subscribe(topologyResponseMessageClassMatchedHandler, net);
    }
}
