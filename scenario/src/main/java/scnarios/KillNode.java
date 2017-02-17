package scnarios;

import se.kth.id2203.networking.NetAddress;
import se.sics.kompics.network.Address;
import se.sics.kompics.simulator.events.system.KillNodeEvent;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * Created by victoraxelsson on 2017-02-17.
 */
public class KillNode extends KillNodeEvent {
    Address selfAdr;

    public KillNode(int port){
        this("127.0.0.1", port);
    }

    public KillNode(String ip, int port){
        try {
            selfAdr = new NetAddress(InetAddress.getByName(ip), port);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Address getNodeAddress() {
        return selfAdr;
    }

    @Override
    public String toString() {
        return "KillNode{" + selfAdr + "}";
    }
}
