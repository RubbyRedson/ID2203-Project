package se.kth.id2203.broadcast.beb;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.kth.id2203.bootstrapping.PutKey;
import se.kth.id2203.broadcast.perfect_link.PL_Deliver;
import se.kth.id2203.broadcast.perfect_link.PL_Send;
import se.kth.id2203.broadcast.perfect_link.PerfectLink;
import se.kth.id2203.networking.Message;
import se.kth.id2203.networking.NetAddress;
import se.sics.kompics.*;
import se.sics.kompics.network.Network;

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
    private NetAddress server = config().getValue("id2203.project.bootstrap-address", NetAddress.class);

    //******* Handlers ******
    protected final Handler<BEB_Broadcast> broadcastHandler = new Handler<BEB_Broadcast>() {
        @Override
        public void handle(BEB_Broadcast beb_broadcast) {
            System.out.println("BEB start in BasicBroadcast");
            for (NetAddress adr : beb_broadcast.topology) {
                System.out.println("BEB trigger to " + adr);
//                PutKey putKey = (PutKey) beb_broadcast.payload;
//                trigger(new Message(self, adr, putKey), net);
                trigger(new PL_Send(self, adr, beb_broadcast.payload), pLink);
            }
        }
    };

    protected final Handler<PL_Deliver> plDeliverHandler = new Handler<PL_Deliver>() {
        @Override
        public void handle(PL_Deliver pl_deliver) {
            System.out.println("PL Deliver received at " + self);
            trigger(new BEB_Deliver(self, pl_deliver.payload), beb);
        }
    };
//    protected final ClassMatchedHandler<PutKey, Message> putKeyHandler = new ClassMatchedHandler<PutKey, Message>() {
//        @Override
//        public void handle(PutKey putKey, Message message) {
//            trigger(new BEB_Deliver(self, putKey), beb);
//        }
//    };



    {
        subscribe(plDeliverHandler, pLink);
        subscribe(broadcastHandler, beb);
    }
}
