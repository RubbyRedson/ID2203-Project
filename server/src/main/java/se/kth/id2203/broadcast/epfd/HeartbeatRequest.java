package se.kth.id2203.broadcast.epfd;

import se.sics.kompics.KompicsEvent;

/**
 * Created by Nick on 2/17/2017.
 */
public class HeartbeatRequest implements KompicsEvent {

    public final int seq;

    public HeartbeatRequest(int seq) {
        this.seq = seq;
    }
}
