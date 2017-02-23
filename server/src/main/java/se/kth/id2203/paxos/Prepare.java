package se.kth.id2203.paxos;

import se.sics.kompics.KompicsEvent;

import java.io.Serializable;

/**
 * Created by victoraxelsson on 2017-02-23.
 */
public class Prepare implements KompicsEvent, Serializable {

    public final int pts;
    public final int al;
    public final int t;


    public Prepare(int pts, int al, int t) {
        this.pts = pts;
        this.al = al;
        this.t = t;
    }

    @Override
    public String toString() {
        return "Prepare{" +
                "pts=" + pts +
                ", al=" + al +
                ", t=" + t +
                '}';
    }
}
