package se.kth.id2203.broadcast.beb;

import se.kth.id2203.networking.NetAddress;
import se.sics.kompics.KompicsEvent;

import java.util.Set;

/**
 * Created by Nick on 2/16/2017.
 */
public class TopologyResponse implements KompicsEvent {
    public final Set<NetAddress> topology;
    public final int topologyId;

    public TopologyResponse(Set<NetAddress> topology, int topologyId) {
        this.topology = topology;
        this.topologyId = topologyId;
    }
}
