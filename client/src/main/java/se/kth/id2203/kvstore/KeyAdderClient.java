package se.kth.id2203.kvstore;

import se.kth.id2203.networking.Message;
import se.kth.id2203.overlay.Connect;
import se.sics.kompics.Handler;
import se.sics.kompics.Start;
import se.sics.kompics.timer.ScheduleTimeout;

/**
 * Created by victoraxelsson on 2017-02-24.
 */
public class KeyAdderClient extends ClientService {

    private String getTestMessage(int seq){
        return "---- SEQ:"+seq+" ---- \n" + self.toString() + "\n------";
    }

    @Override
    protected void doStartupStuff() {
        Operation putKey1 = new PutOperation("port" + self.getPort(), getTestMessage(1));
        trigger(new Message(self, server, putKey1), net);

        Operation putKey2 = new PutOperation("port" + self.getPort(), getTestMessage(2));
        trigger(new Message(self, server, putKey2), net);

        Operation putKey3 = new PutOperation("port" + self.getPort(), getTestMessage(3));
        trigger(new Message(self, server, putKey3), net);

    }
}
