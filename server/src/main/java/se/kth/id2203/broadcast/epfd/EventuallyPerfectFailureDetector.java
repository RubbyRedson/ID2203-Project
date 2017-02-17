package se.kth.id2203.broadcast.epfd;

import se.kth.id2203.broadcast.beb.BEB_Deliver;
import se.sics.kompics.PortType;

/**
 * Created by Nick on 2/17/2017.
 */
public class EventuallyPerfectFailureDetector extends PortType {
    {
        indication(Suspect.class);
        indication(Restore.class);

    }
}
