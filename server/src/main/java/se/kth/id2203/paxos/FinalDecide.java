package se.kth.id2203.paxos;

import se.kth.id2203.kvstore.Operation;
import se.sics.kompics.KompicsEvent;

import java.io.Serializable;

/**
 * Created by victoraxelsson on 2017-02-23.
 */
public class FinalDecide implements KompicsEvent, Serializable {

    public final Operation operation;


    public FinalDecide(Operation operation) {
        this.operation = operation;
    }

    @Override
    public String toString() {
        return "FinalDecide{" +
                "operation=" + operation +
                '}';
    }
}
