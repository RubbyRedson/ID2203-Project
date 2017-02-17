package se.kth.id2203.broadcast.epfd;

import se.sics.kompics.timer.SchedulePeriodicTimeout;
import se.sics.kompics.timer.ScheduleTimeout;

/**
 * Created by Nick on 2/17/2017.
 */
public class Timeout extends se.sics.kompics.timer.Timeout {
    protected Timeout(ScheduleTimeout request) {
        super(request);
    }
}
