package se.kth.id2203.bootstrapping;

import se.kth.id2203.networking.NetAddress;
import se.sics.kompics.KompicsEvent;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by victoraxelsson on 2017-02-16.
 */
public class PutKey implements KompicsEvent {

    public final String key;
    public final String val;
    public final NetAddress client;
    public Set<NetAddress> topology;

    public PutKey(String key, String val, NetAddress client) {
        this.key = key;
        this.val = val;
        this.client = client;
    }

    public void setTopology(Set<NetAddress> topology) {
        this.topology = topology;
    }
}
