package se.kth.id2203.broadcast.beb;

import se.kth.id2203.networking.NetAddress;
import se.sics.kompics.KompicsEvent;
import se.sics.kompics.network.Address;

import java.io.Serializable;
import java.util.Set;

/**
 * Created by Nick on 2/16/2017.
 */
public class BEB_Broadcast implements KompicsEvent, Serializable {
    public final KompicsEvent payload;
    public final Set<NetAddress> topology;

    public BEB_Broadcast(KompicsEvent payload, Set<NetAddress> topology) {
        this.payload = payload;
        this.topology = topology; 
    }

    @Override
    public String toString() {
        return "BEB_Broadcast{" +
                ", payload=" + payload +
                '}';
    }
}
