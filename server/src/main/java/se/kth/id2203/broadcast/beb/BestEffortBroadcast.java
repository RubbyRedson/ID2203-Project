package se.kth.id2203.broadcast.beb;

import se.sics.kompics.PortType;

/**
 * Created by Nick on 2/16/2017.
 */
public class BestEffortBroadcast extends PortType {
    {
        request(BEB_Broadcast.class);
        indication(BEB_Deliver.class);
    }

}
