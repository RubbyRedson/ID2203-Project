package se.kth.id2203.paxos;

import se.kth.id2203.kvstore.Operation;
import se.sics.kompics.KompicsEvent;

import java.io.Serializable;
import java.util.List;

/**
 * Created by victoraxelsson on 2017-02-23.
 */
public class PrepareAck implements KompicsEvent, Serializable{

    public final int ts, ats, al, t;
    public final List<Operation> vsuf;
    public final int partitionId;


    public PrepareAck(int ts, int ats, int al, int t, List<Operation> vsuf, int partitionId) {
        this.ts = ts;
        this.ats = ats;
        this.al = al;
        this.t = t;
        this.vsuf = vsuf;
        this.partitionId = partitionId;
    }


    @Override
    public String toString() {
        return "PrepareAck{" +
                "ts=" + ts +
                ", ats=" + ats +
                ", al=" + al +
                ", t=" + t +
                ", vsuf=" + vsuf +
                '}';
    }
}
