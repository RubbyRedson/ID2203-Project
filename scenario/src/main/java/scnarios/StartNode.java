package scnarios;

import se.kth.id2203.kvstore.ClientService;
import se.kth.id2203.networking.NetAddress;
import se.sics.kompics.ComponentDefinition;
import se.sics.kompics.Init;
import se.sics.kompics.Start;
import se.sics.kompics.network.Address;
import se.sics.kompics.simulator.events.system.StartNodeEvent;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by victoraxelsson on 2017-02-16.
 */
public class StartNode extends StartNodeEvent{

    NetAddress selfAdr;
    Class theClass;
    int port;
    int masterPort;

    public static final int MASTER_DEFAULT_PORT = 45678;

    public StartNode(Class theClass){
        this(theClass, getFreePort());
    }

    public StartNode(Class theClass, int port){
        this(theClass, "127.0.0.1", port);
    }


    public StartNode(Class theClass, String ip, int port){
        this(theClass, ip, port, MASTER_DEFAULT_PORT);
    }

    public StartNode(Class theClass, String ip, int port, int masterPort){
        this.theClass = theClass;
        this.port = port;
        this.masterPort = masterPort;

        try {
            selfAdr = new NetAddress(InetAddress.getByName(ip), port);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }

    /*
    private int[] getFreePorts(int count) {
        int[] ports = new int[count];
        for(int i = 0; i < count; i++){
            ports[i] = getFreePort();
        }

        return ports;
    }
    */

    private static int getFreePort(){
        try {
            ServerSocket s = new ServerSocket(0);
            return s.getLocalPort();
        } catch (IOException ex) {
            return -1;
        }
    }

    @Override
    public Map<String, Object> initConfigUpdate() {
        HashMap<String, Object> config = new HashMap<>();
        getNodeAddress();

        config.put("id2203.project.address", selfAdr);

        if(masterPort > 0){
            config.put("id2203.project.bootstrap-address",  new NetAddress(selfAdr.getIp(), masterPort));
        }

        return config;
    }

    @Override
    public Address getNodeAddress() {
        return selfAdr;
    }
    @Override
    public Class getComponentDefinition() {
        return theClass;
    }

    @Override
    public Init getComponentInit() {
        return Init.NONE;
    }

    @Override
    public String toString() {
        return theClass.toString() + "<" + selfAdr.toString() + ">";
    }
}
