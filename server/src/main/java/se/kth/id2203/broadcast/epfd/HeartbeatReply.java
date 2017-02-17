package se.kth.id2203.broadcast.epfd;

import se.sics.kompics.KompicsEvent;

/**
 * Created by Nick on 2/17/2017.
 */
public class HeartbeatReply implements KompicsEvent {
    public final int seq;

    public HeartbeatReply(int seq) {
        this.seq = seq;
    }

    @Override
    public String toString() {
        return "HeartbeatReply{" +
                "seq=" + seq +
                '}';
    }
}
