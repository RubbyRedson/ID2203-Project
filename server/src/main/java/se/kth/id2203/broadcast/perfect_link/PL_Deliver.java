package se.kth.id2203.broadcast.perfect_link;

import se.kth.id2203.networking.NetAddress;
import se.sics.kompics.KompicsEvent;
import se.sics.kompics.network.Address;

import java.io.Serializable;

/**
 * Created by Nick on 2/16/2017.
 */
public class PL_Deliver implements KompicsEvent, Serializable {
    public final NetAddress src;
    public final NetAddress to;
    public final KompicsEvent payload;

    public PL_Deliver(NetAddress src, NetAddress to, KompicsEvent payload) {
        this.src = src;
        this.to = to;
        this.payload = payload;
    }

    @Override
    public String toString() {
        return "PL_Deliver{" +
                "src=" + src +
                ", to=" + to +
                ", payload=" + payload +
                '}';
    }
}