package se.kth.id2203.bootstrapping;

import se.sics.kompics.KompicsEvent;

/**
 * Created by victoraxelsson on 2017-02-16.
 */
public class PutKey implements KompicsEvent {

    String key;
    String val;

    public void putKey(String key, String val){
        this.key = key;
        this.val = val;
    }
}
