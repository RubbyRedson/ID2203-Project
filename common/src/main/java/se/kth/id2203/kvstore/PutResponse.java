package se.kth.id2203.kvstore;

import se.sics.kompics.KompicsEvent;

import java.io.Serializable;
import java.util.UUID;

/**
 * Created by Nick on 2/23/2017.
 */
public class PutResponse extends OpResponse {
    public PutResponse(UUID id, Code status) {
        super(id, status);
    }
}
