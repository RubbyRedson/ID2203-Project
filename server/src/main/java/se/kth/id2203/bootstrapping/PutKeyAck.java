package se.kth.id2203.bootstrapping;

import se.sics.kompics.KompicsEvent;

/**
 * Created by victoraxelsson on 2017-02-16.
 */
public class PutKeyAck implements KompicsEvent{
    public String key;

    public PutKeyAck(String key){
        this.key = key;
    }
}
