package se.kth.id2203.broadcast.beb;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.kth.id2203.broadcast.perfect_link.PL_Deliver;
import se.kth.id2203.broadcast.perfect_link.PL_Send;
import se.kth.id2203.broadcast.perfect_link.PerfectLink;
import se.kth.id2203.networking.NetAddress;
import se.sics.kompics.*;

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
    private NetAddress server = config().getValue("id2203.project.bootstrap-address", NetAddress.class);

    //******* Handlers ******
    protected final Handler<BEB_Broadcast> broadcastHandler = new Handler<BEB_Broadcast>() {
        @Override
        public void handle(BEB_Broadcast beb_broadcast) {
            System.out.println("BEB start in BasicBroadcast");
            for (NetAddress adr : beb_broadcast.topology) {
                System.out.println("BEB trigger to " + adr);
                trigger(new PL_Send(self, adr, beb_broadcast), pLink);
            }
        }
    };

    protected final ClassMatchedHandler<BEB_Broadcast, PL_Deliver> plDeliverHandler = new ClassMatchedHandler<BEB_Broadcast, PL_Deliver>() {
        @Override
        public void handle(BEB_Broadcast beb_broadcast, PL_Deliver pl_deliver) {
            System.out.println("PL Deliver received at " + self);
            trigger(new BEB_Deliver(self, ((BEB_Broadcast)pl_deliver.payload).payload), beb);
        }
    };



    {
        subscribe(plDeliverHandler, pLink);
        subscribe(broadcastHandler, beb);
    }
}
