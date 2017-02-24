package se.kth.id2203.kvstore;

import java.util.UUID;

/**
 * Created by Nick on 2/24/2017.
 */
public class CasResponse extends OpResponse {
    public CasResponse(UUID id, Code status) {
        super(id, status);
    }
}
