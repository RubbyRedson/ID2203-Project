package se.kth.id2203.paxos;

import se.kth.id2203.kvstore.Operation;
import se.sics.kompics.KompicsEvent;

import java.io.Serializable;
import java.util.List;

/**
 * Created by victoraxelsson on 2017-02-23.
 */
public class Accept implements KompicsEvent, Serializable{

    public final int pts;
    public final List<Operation> vsuff;
    public final int offs;
    public final int t;
    public final int partitionId;


    public Accept(int pts, List<Operation> vsuff, int offs, int t, int partitionId) {
        this.pts = pts;
        this.vsuff = vsuff;
        this.offs = offs;
        this.t = t;
        this.partitionId = partitionId;
    }

    @Override
    public String toString() {
        return "Accept{" +
                "pts=" + pts +
                ", vsuff=" + vsuff +
                ", offs=" + offs +
                ", t=" + t +
                '}';
    }
}
