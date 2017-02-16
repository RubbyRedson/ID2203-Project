package se.kth.id2203.broadcast.beb;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.kth.id2203.bootstrapping.PutKey;
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
    protected final Positive<Network> net = requires(Network.class);
    protected final Negative<BestEffortBroadcast> beb = provides(BestEffortBroadcast.class);
    //******* Fields ******
    private NetAddress self = config().getValue("id2203.project.address", NetAddress.class);
    private NetAddress server = config().getValue("id2203.project.bootstrap-address", NetAddress.class);
    private Set<NetAddress> topology = new HashSet<>();

    //******* Handlers ******
    protected final Handler<Start> startHandler = new Handler<Start>() {
        @Override
        public void handle(Start start) {
            System.out.println("in the \"ctor\" ");
            topology = config().getValue("id2203.project.topology", Set.class);
            trigger(new Message(self, server, new TopologyQuery()), net);
        }
    };

    protected final Handler<BEB_Broadcast> broadcastHandler = new Handler<BEB_Broadcast>() {
        @Override
        public void handle(BEB_Broadcast beb_broadcast) {
            for (NetAddress adr : topology) {
                trigger(new Message(self, adr, beb_broadcast), net);
            }
        }
    };
    protected final ClassMatchedHandler<PutKey, Message> putKeyHandler = new ClassMatchedHandler<PutKey, Message>() {
        @Override
        public void handle(PutKey putKey, Message message) {
            trigger(new BEB_Deliver(self, putKey), beb);
        }
    };

    protected final ClassMatchedHandler<TopologyResponse, Message> topologyResponseMessageClassMatchedHandler = new ClassMatchedHandler<TopologyResponse, Message>() {
        @Override
        public void handle(TopologyResponse topologyResponse, Message message) {
            topology = topologyResponse.topology;
        }
    };


    {
        subscribe(startHandler, control);
        subscribe(putKeyHandler, net);
        subscribe(topologyResponseMessageClassMatchedHandler, net);
        subscribe(broadcastHandler, beb);
    }
}
