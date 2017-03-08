package se.kth.id2203.kvstore;

import java.util.UUID;

/**
 * Created by Nick on 2/24/2017.
 */
public class CasResponse extends OpResponse {
    public final String value;

    public CasResponse(UUID id, Code status, String value) {
        super(id, status);
        this.value = value;
    }
}
