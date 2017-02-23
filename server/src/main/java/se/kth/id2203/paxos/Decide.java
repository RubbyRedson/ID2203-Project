package se.kth.id2203.paxos;

import se.sics.kompics.KompicsEvent;

import java.io.Serializable;

/**
 * Created by Nick on 2/23/2017.
 */
public class Decide implements KompicsEvent, Serializable {
    public final int pts, pl, t;

    public Decide(int pts, int pl, int t) {
        this.pts = pts;
        this.pl = pl;
        this.t = t;
    }

    @Override
    public String toString() {
        return "Decide{" +
                "pts=" + pts +
                ", pl=" + pl +
                ", t=" + t +
                '}';
    }
}
