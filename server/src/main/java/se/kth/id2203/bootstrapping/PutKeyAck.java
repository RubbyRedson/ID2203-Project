package se.kth.id2203.bootstrapping;

import se.kth.id2203.networking.NetAddress;
import se.sics.kompics.KompicsEvent;

/**
 * Created by victoraxelsson on 2017-02-16.
 */
public class PutKeyAck implements KompicsEvent{
    public String key;
    public final NetAddress to;

    public PutKeyAck(String key, NetAddress to){
        this.key = key;
        this.to = to;
    }
}
