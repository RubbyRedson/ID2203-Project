package se.kth.id2203.paxos;

import se.sics.kompics.KompicsEvent;

import java.io.Serializable;

/**
 * Created by Nick on 2/23/2017.
 */
public class Abort implements KompicsEvent, Serializable {
    public final String value;
    public final int partitionId;

    public Abort(String value, int partitionId) {
        this.value = value;
        this.partitionId = partitionId;
    }

    @Override
    public String toString() {
        return "Abort{" +
                "value='" + value + '\'' +
                '}';
    }
}
