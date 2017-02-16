package se.kth.id2203.bootstrapping;

import se.sics.kompics.KompicsEvent;

/**
 * Created by victoraxelsson on 2017-02-16.
 */
public class PutKey implements KompicsEvent {

    public final String key;
    public final String val;

    public PutKey(String key, String val) {
        this.key = key;
        this.val = val;
    }
}
