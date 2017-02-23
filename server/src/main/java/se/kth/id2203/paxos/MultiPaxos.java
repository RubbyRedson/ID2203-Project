package se.kth.id2203.paxos;

import se.sics.kompics.PortType;

/**
 * Created by Nick on 2/23/2017.
 */
public class MultiPaxos extends PortType {
    {
        request(Propose.class);
        indication(Decide.class);
        indication(Abort.class);
    }
}
