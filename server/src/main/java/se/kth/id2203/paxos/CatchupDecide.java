package se.kth.id2203.paxos;

import se.kth.id2203.kvstore.Operation;
import se.sics.kompics.KompicsEvent;

import java.io.Serializable;
import java.util.List;

/**
 * Created by victoraxelsson on 2017-02-24.
 */
public class CatchupDecide implements KompicsEvent, Serializable {

    public final int ts;
    public final int cfg;
    public final List<Operation> vd;

    public CatchupDecide(int ts, int cfg, List<Operation> vd) {
        this.ts = ts;
        this.cfg = cfg;
        this.vd = vd;
    }

    @Override
    public String toString() {
        return "CatchupDecide{" +
                "vd=" + vd +
                '}';
    }
}
