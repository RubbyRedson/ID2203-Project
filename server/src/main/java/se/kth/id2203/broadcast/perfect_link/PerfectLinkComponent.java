package se.kth.id2203.broadcast.perfect_link;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.kth.id2203.broadcast.beb.BasicBroadcast;
import se.kth.id2203.networking.Message;
import se.sics.kompics.ComponentDefinition;
import se.sics.kompics.Handler;
import se.sics.kompics.Negative;
import se.sics.kompics.Positive;
import se.sics.kompics.network.Network;

/**
 * Created by Nick on 2/16/2017.
 */
public class PerfectLinkComponent extends ComponentDefinition {
    final static Logger LOG = LoggerFactory.getLogger(BasicBroadcast.class);
    //******* Ports ******
    protected final Positive<Network> network = requires(Network.class);
    protected final Negative<PerfectLink> pLink = provides(PerfectLink.class);

    protected final Handler<PL_Send> sendHandler = new Handler<PL_Send>() {
        @Override
        public void handle(PL_Send pl_send) {
            trigger(new Message(pl_send.src, pl_send.to, pl_send.payload), network);
        }
    };
}
