package se.kth.id2203.kvstore;

import se.kth.id2203.networking.Message;

/**
 * Created by Nick on 2/24/2017.
 */
public class ClientServiceWithCasTesting extends ClientService {
    @Override
    protected void doStartupStuff() {
        Operation putKey = new PutOperation("23hash23", "0");
        trigger(new Message(self, server, putKey), net);

        for (int i = 0; i < 5; i++) {
            Operation cas = new CasOperation("23hash23", i + "", (i + 1) + "");
            trigger(new Message(self, server, cas), net);
        }

        Operation casNotOk = new CasOperation("23hash23", "0", "some new stuff");
        trigger(new Message(self, server, casNotOk), net);
    }
}
