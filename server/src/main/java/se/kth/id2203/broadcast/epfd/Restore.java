package se.kth.id2203.broadcast.epfd;

import se.kth.id2203.networking.NetAddress;
import se.sics.kompics.KompicsEvent;

/**
 * Created by Nick on 2/17/2017.
 */
public class Restore implements KompicsEvent {
    public final NetAddress p;

    public Restore(NetAddress p) {
        this.p = p;
    }
}
