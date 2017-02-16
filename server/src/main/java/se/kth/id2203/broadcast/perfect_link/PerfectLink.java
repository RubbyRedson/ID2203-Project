package se.kth.id2203.broadcast.perfect_link;

import se.sics.kompics.PortType;

/**
 * Created by Nick on 2/16/2017.
 */
public class PerfectLink extends PortType {
    {
        request(PL_Send.class);
        indication(PL_Deliver.class);
    }

}
