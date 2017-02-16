import org.apache.commons.cli.*;
import se.kth.id2203.HostComponent;
import se.kth.id2203.networking.NetAddress;
import se.kth.id2203.networking.NetAddressConverter;
import se.sics.kompics.Kompics;
import se.sics.kompics.config.Config;
import se.sics.kompics.config.ConfigUpdate;
import se.sics.kompics.config.Conversions;
import se.sics.kompics.config.ValueMerger;

import java.io.IOException;
import java.io.Serializable;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.UnknownHostException;
import java.util.UUID;

/**
 * Created by victoraxelsson on 2017-02-16.
 */
public class Main {


    public static void main(String[] args) {
        String[] leader = {};
        se.kth.id2203.Main.main(leader);
        startSlaves(5, "127.0.0.1:45678", -1);
    }

    private static int[] getFreePorts(int count) {
        int[] ports = new int[count];
        for(int i = 0; i < count; i++){
            ports[i] = getFreePort();
        }

        return ports;
    }

    private static int getFreePort(){
        try {
            ServerSocket s = new ServerSocket(0);
            return s.getLocalPort();
        } catch (IOException ex) {
            return -1;
        }
    }

    private static void startSlaves(int count, String ipAndPort, int delay){

        int[] ports = getFreePorts(count);

        for (int i = 0; i < count; i++){

            String[] slave = {"-c", ipAndPort, "-p", ports[i] + ""};
            se.kth.id2203.Main.main(slave);

            if(delay > 0){
                try {
                    Thread.sleep(delay);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

}
