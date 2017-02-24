package se.kth.id2203.kvstore;

import se.kth.id2203.networking.NetAddress;

import java.util.Set;

/**
 * Created by Nick on 2/24/2017.
 */
public class StopSignOperation extends Operation {
    public final Set<NetAddress> topology;
    public final int cfg;

    public StopSignOperation(Set<NetAddress> topology, int cfg) {
        super(null);
        this.topology = topology;
        this.cfg = cfg;
    }
}
