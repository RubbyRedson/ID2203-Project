package se.kth.id2203;

import com.google.common.base.Optional;
import se.kth.id2203.nodes.Slave;
import se.kth.id2203.nodes.Master;
import se.kth.id2203.broadcast.beb.BasicBroadcast;
import se.kth.id2203.broadcast.beb.BestEffortBroadcast;
import se.kth.id2203.broadcast.epfd.EpfdComponent;
import se.kth.id2203.broadcast.epfd.EventuallyPerfectFailureDetector;
import se.kth.id2203.broadcast.perfect_link.PerfectLink;
import se.kth.id2203.broadcast.perfect_link.PerfectLinkComponent;
import se.kth.id2203.networking.NetAddress;
import se.kth.id2203.paxos.MultiPaxos;
import se.kth.id2203.paxos.MultiPaxosComponent;
import se.kth.id2203.paxos.PaxosInit;
import se.sics.kompics.*;
import se.sics.kompics.network.Network;
import se.sics.kompics.timer.Timer;

public class ParentComponent
        extends ComponentDefinition {

    //******* Ports ******
    protected final Positive<Network> net = requires(Network.class);
    protected final Positive<Timer> timer = requires(Timer.class);
    //******* Children ******
//    protected final Component overlay = create(VSOverlayManager.class, Init.NONE);
//    protected final Component kv = create(KVService.class, Init.NONE);
    protected final Component boot;
    private Component basicBroadcast = create(BasicBroadcast.class, Init.NONE);
    private Component pLink = create(PerfectLinkComponent.class, Init.NONE);
    private Component epfd = create(EpfdComponent.class, Init.NONE);

    {

        Optional<NetAddress> serverO = config().readValue("id2203.project.bootstrap-address", NetAddress.class);
        if (serverO.isPresent()) { // start in client mode
            boot = create(Slave.class, Init.NONE);
            connect(basicBroadcast.getPositive(BestEffortBroadcast.class), boot.getNegative(BestEffortBroadcast.class)
                    , Channel.TWO_WAY);
            Component paxos = create(MultiPaxosComponent.class, Init.NONE);
            connect(pLink.getPositive(PerfectLink.class), paxos.getNegative(PerfectLink.class), Channel.TWO_WAY);
            connect(paxos.getPositive(MultiPaxos.class),
                    boot.getNegative(MultiPaxos.class), Channel.TWO_WAY);
            connect(net, paxos.getNegative(Network.class), Channel.TWO_WAY);
        } else { // start in server mode
            boot = create(Master.class, Init.NONE);
            // Paxos
            for (int i = 0; i < Master.PARTITION_COUNT; i++) {
                Component paxos = create(MultiPaxosComponent.class, new PaxosInit(i));
                connect(pLink.getPositive(PerfectLink.class), paxos.getNegative(PerfectLink.class), Channel.TWO_WAY);
                connect(paxos.getPositive(MultiPaxos.class),
                        boot.getNegative(MultiPaxos.class), Channel.TWO_WAY);
                connect(net, paxos.getNegative(Network.class), Channel.TWO_WAY);
            }
        }

        connect(timer, boot.getNegative(Timer.class), Channel.TWO_WAY);
        connect(net, boot.getNegative(Network.class), Channel.TWO_WAY);
        // Overlay
//        connect(boot.getPositive(Bootstrapping.class), overlay.getNegative(Bootstrapping.class), Channel.TWO_WAY);
//        connect(net, overlay.getNegative(Network.class), Channel.TWO_WAY);
        // KV
//        connect(overlay.getPositive(Routing.class), kv.getNegative(Routing.class), Channel.TWO_WAY);
//        connect(net, kv.getNegative(Network.class), Channel.TWO_WAY);
        // Perfect Link
        connect(pLink.getPositive(PerfectLink.class), basicBroadcast.getNegative(PerfectLink.class), Channel.TWO_WAY);
        connect(net, pLink.getNegative(Network.class), Channel.TWO_WAY);

        // EPFD
        connect(epfd.getPositive(EventuallyPerfectFailureDetector.class),
                boot.getNegative(EventuallyPerfectFailureDetector.class), Channel.TWO_WAY);
        connect(net, epfd.getNegative(Network.class), Channel.TWO_WAY);
        connect(pLink.getPositive(PerfectLink.class), epfd.getNegative(PerfectLink.class), Channel.TWO_WAY);
        connect(timer, epfd.getNegative(Timer.class), Channel.TWO_WAY);


    }
}
