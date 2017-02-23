package se.kth.id2203.kvstore;

import java.util.UUID;

/**
 * Created by victoraxelsson on 2017-02-23.
 */
public class GetResponse extends OpResponse {

    public final String value;

    public GetResponse(UUID id, Code status, String value) {
        super(id, status);
        this.value = value;
    }
}
