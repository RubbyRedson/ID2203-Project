package se.kth.id2203.broadcast.beb;

import se.kth.id2203.networking.NetAddress;
import se.sics.kompics.KompicsEvent;
import se.sics.kompics.network.Address;

import java.io.Serializable;

/**
 * Created by Nick on 2/16/2017.
 */
public class BEB_Deliver implements KompicsEvent, Serializable {
    public final NetAddress src;
    public final KompicsEvent payload;

    public BEB_Deliver(NetAddress src, KompicsEvent payload) {
        this.src = src;
        this.payload = payload;
    }

    @Override
    public String toString() {
        return "BEB_Deliver{" +
                "src=" + src +
                ", payload=" + payload +
                '}';
    }
}
