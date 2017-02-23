package se.kth.id2203.kvstore;

/**
 * Created by victoraxelsson on 2017-02-23.
 */
public class PutOperation extends Operation {

    public final String value;

    public PutOperation(String key, String value) {
        super(key);
        this.value = value;
    }
}
