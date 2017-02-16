package se.kth.id2203.broadcast.beb;

import se.kth.id2203.networking.NetAddress;
import se.sics.kompics.KompicsEvent;

import java.util.Set;

/**
 * Created by Nick on 2/16/2017.
 */
public class BEB_Init implements KompicsEvent {
    final NetAddress self;
    final Set<NetAddress> topology;

    public BEB_Init(NetAddress self, Set<NetAddress> topology) {
        this.self = self;
        this.topology = topology;
    }

    @Override
    public String toString() {
        return "BEB_Init{" +
                "self=" + self +
                ", topology=" + topology +
                '}';
    }
}
