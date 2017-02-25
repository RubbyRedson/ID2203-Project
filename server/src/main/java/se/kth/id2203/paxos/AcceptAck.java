package se.kth.id2203.paxos;

import se.sics.kompics.KompicsEvent;

import java.io.Serializable;

/**
 * Created by victoraxelsson on 2017-02-23.
 */
public class AcceptAck implements KompicsEvent, Serializable {

    public final int pts, l, t;
    public final int partitionId;


    public AcceptAck(int pts, int l, int t, int partitionId) {
        this.pts = pts;
        this.l = l;
        this.t = t;
        this.partitionId = partitionId;
    }

    @Override
    public String toString() {
        return "AcceptAck{" +
                "pts=" + pts +
                ", l=" + l +
                ", t=" + t +
                '}';
    }
}
