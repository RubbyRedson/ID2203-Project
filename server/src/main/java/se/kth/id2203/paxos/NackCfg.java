package se.kth.id2203.paxos;

import se.sics.kompics.KompicsEvent;

import java.io.Serializable;

/**
 * Created by Nick on 2/24/2017.
 */
public class NackCfg implements KompicsEvent, Serializable {
    public final int cfg, ts;
    public final int partitionId;

    public NackCfg(int cfg, int ts, int partitionId) {
        this.cfg = cfg;
        this.ts = ts;
        this.partitionId = partitionId;
    }

    @Override
    public String toString() {
        return "NackCfg{" +
                "cfg=" + cfg +
                ", ts=" + ts +
                '}';
    }
}
