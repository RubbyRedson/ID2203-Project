package se.kth.id2203.paxos;

import se.sics.kompics.KompicsEvent;

import java.io.Serializable;

/**
 * Created by victoraxelsson on 2017-02-23.
 */
public class Nack implements KompicsEvent, Serializable {

    public final int ts, t;
    public final int partitionId;


    public Nack(int ts, int t, int partitionId) {
        this.ts = ts;
        this.t = t;
        this.partitionId = partitionId;
    }

    @Override
    public String toString() {
        return "Nack{" +
                "ts=" + ts +
                ", t=" + t +
                '}';
    }
}
