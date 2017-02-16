package se.kth.id2203.broadcast.beb;

import se.sics.kompics.KompicsEvent;
import se.sics.kompics.network.Address;

import java.io.Serializable;

/**
 * Created by Nick on 2/16/2017.
 */
public class BEB_Broadcast implements KompicsEvent, Serializable {
    public final KompicsEvent payload;

    public BEB_Broadcast(KompicsEvent payload) {
        this.payload = payload;
    }

    @Override
    public String toString() {
        return "BEB_Broadcast{" +
                ", payload=" + payload +
                '}';
    }
}
