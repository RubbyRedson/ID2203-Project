package se.kth.id2203.broadcast.perfect_link;

import se.kth.id2203.networking.NetAddress;
import se.sics.kompics.KompicsEvent;
import se.sics.kompics.PatternExtractor;

import java.io.Serializable;

/**
 * Created by Nick on 2/16/2017.
 */
public class PL_Deliver implements KompicsEvent, Serializable, PatternExtractor<Class, KompicsEvent> {
    public final NetAddress src;
    public final KompicsEvent payload;

    public PL_Deliver(NetAddress src, KompicsEvent payload) {
        this.src = src;
        this.payload = payload;
    }

    @Override
    public String toString() {
        return "PL_Deliver{" +
                "src=" + src +
                ", payload=" + payload +
                '}';
    }

    @Override
    public Class extractPattern() {
        return payload.getClass();
    }

    @Override
    public KompicsEvent extractValue() {
        return payload;
    }
}
