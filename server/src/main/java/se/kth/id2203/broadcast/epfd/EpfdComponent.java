package se.kth.id2203.broadcast.epfd;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.kth.id2203.broadcast.perfect_link.PL_Deliver;
import se.kth.id2203.broadcast.perfect_link.PL_Send;
import se.kth.id2203.broadcast.perfect_link.PerfectLink;
import se.kth.id2203.networking.NetAddress;
import se.sics.kompics.*;
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
    protected final Negative<EventuallyPerfectFailureDetector> epfd = provides(EventuallyPerfectFailureDetector.class);
    //******* Fields ******
    private NetAddress self = config().getValue("id2203.project.address", NetAddress.class);
    private static int DELAY = 200;
    private int delay = 200;
    private Set<NetAddress> topology = new HashSet<>();

    private Set<NetAddress> alive = new HashSet<>(); //TODO init with topology
    private Set<NetAddress> suspected = new HashSet<>();
    private int seqnum = 0;

    protected final Handler<Start> startHandler = new Handler<Start>() {
        @Override
        public void handle(Start e) {
            ScheduleTimeout spt = new ScheduleTimeout(delay);
            spt.setTimeoutEvent(new Timeout(spt));
            trigger(spt, timer);

        }
    };
    protected final Handler<Timeout> timeoutHandler = new Handler<Timeout>() {
        @Override
        public void handle(Timeout e) {
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
            trigger(new PL_Send(self, pl_deliver.src, new HeartbeatReply(seqnum)), pLink);
        }
    };
    protected final ClassMatchedHandler<HeartbeatReply, PL_Deliver> plDeliverResponseHandler = new ClassMatchedHandler<HeartbeatReply, PL_Deliver>() {
        @Override
        public void handle(HeartbeatReply heartbeatReply, PL_Deliver pl_deliver) {
            if (seqnum == heartbeatReply.seq || suspected.contains(pl_deliver.src)) {
                alive.add(pl_deliver.src);
            }
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

    {
        subscribe(startHandler, control);
        subscribe(timeoutHandler, timer);
        subscribe(plDeliverRequestHandler, pLink);
        subscribe(plDeliverResponseHandler, pLink);
    }
}
