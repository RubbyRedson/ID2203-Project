package se.kth.id2203.paxos;

import se.sics.kompics.KompicsEvent;

import java.io.Serializable;

/**
 * Created by Nick on 2/23/2017.
 */
public class Propose implements KompicsEvent, Serializable {
    public final String value;

    public Propose(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return "Propose{" +
                "value='" + value + '\'' +
                '}';
    }
}
