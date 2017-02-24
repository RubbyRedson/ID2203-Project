package se.kth.id2203.paxos;

import se.sics.kompics.Init;

/**
 * Created by Nick on 2/24/2017.
 */
public class PaxosInit extends Init<MultiPaxosComponent> {
    public final int partitionId;

    public PaxosInit(int partitionId) {
        this.partitionId = partitionId;
    }
}
