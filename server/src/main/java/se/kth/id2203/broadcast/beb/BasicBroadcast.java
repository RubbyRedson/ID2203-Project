package se.kth.id2203.broadcast.beb;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.kth.id2203.broadcast.perfect_link.PL_Deliver;
import se.kth.id2203.broadcast.perfect_link.PerfectLink;
import se.kth.id2203.networking.Message;
import se.kth.id2203.networking.NetAddress;
import se.sics.kompics.ComponentDefinition;
import se.sics.kompics.Handler;
import se.sics.kompics.Negative;
import se.sics.kompics.Positive;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by Nick on 2/16/2017.
 */
public class BasicBroadcast extends ComponentDefinition {

    final static Logger LOG = LoggerFactory.getLogger(BasicBroadcast.class);
    //******* Ports ******
    protected final Positive<PerfectLink> pLink = requires(PerfectLink.class);
    protected final Negative<BestEffortBroadcast> beb = provides(BestEffortBroadcast.class);
    //******* Fields ******
    private NetAddress self = config().getValue("id2203.project.address", NetAddress.class);
    private Set<NetAddress> topology = new HashSet<>();

    //******* Handlers ******
    protected final Handler<BEB_Init> initHandler = new Handler<BEB_Init>() {
        @Override
        public void handle(BEB_Init BEBInit) {
            self = BEBInit.self;
            topology = BEBInit.topology;
            LOG.info("Starting Best Effort Broadcast {} with topology {}", self, BEBInit.topology);
        }
    };
    protected final Handler<BEB_Broadcast> broadcastHandler = new Handler<BEB_Broadcast>() {
        @Override
        public void handle(BEB_Broadcast beb_broadcast) {
            for (NetAddress adr : topology) {
                trigger(new Message(self, adr, beb_broadcast), pLink);
            }
        }
    };
    protected final Handler<PL_Deliver> plDeliverHandler = new Handler<PL_Deliver>() {
        @Override
        public void handle(PL_Deliver pl_deliver) {
            trigger(new BEB_Deliver(self, pl_deliver.payload), beb);
        }
    };




}
