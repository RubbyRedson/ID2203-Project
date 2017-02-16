package se.kth.id2203.bootstrapping;

import se.sics.kompics.KompicsEvent;

/**
 * Created by victoraxelsson on 2017-02-16.
 */
public class AddKey implements KompicsEvent {

    String key;

    public void addKey(String key){
        this.key = key;
    }
}
