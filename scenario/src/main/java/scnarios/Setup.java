package scnarios;

import se.kth.id2203.HostComponent;
import se.kth.id2203.networking.NetAddress;
import se.sics.kompics.Init;
import se.sics.kompics.network.Address;
import se.sics.kompics.simulator.SimulationScenario;
import se.sics.kompics.simulator.adaptor.Operation1;
import se.sics.kompics.simulator.adaptor.distributions.extra.BasicIntSequentialDistribution;
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
public class Setup {

    public static SimulationScenario simpleSetup() {
        SimulationScenario scen = new SimulationScenario() {
            {
                SimulationScenario.StochasticProcess setup = new SimulationScenario.StochasticProcess() {
                    {
                        eventInterArrivalTime(constant(1000));
                        raise(1, startSetup, new BasicIntSequentialDistribution(2));
                    }
                };

                setup.start();
                terminateAfterTerminationOf(2000, setup);

                //ponger.start();
                //pinger.startAfterTerminationOf(1000, ponger);
                //terminateAfterTerminationOf(10000, pinger);
            }
        };

        return scen;
    }

    public static Operation1 startSetup = new Operation1<StartNodeEvent, Integer>() {

        @Override
        public StartNodeEvent generate(final Integer self) {
            return new StartNodeEvent() {

                NetAddress selfAdr;

                @Override
                public Address getNodeAddress() {
                    InetAddress ad = null;
                    try {
                       ad = InetAddress.getLocalHost();
                    } catch (UnknownHostException e) {
                        e.printStackTrace();
                    }

                    selfAdr = new NetAddress(ad, 45678);
                    return selfAdr;
                }

                @Override
                public Class getComponentDefinition() {
                    return HostComponent.class;
                }

                @Override
                public Init getComponentInit() {
                    return Init.NONE;
                }

                @Override
                public Map<String, Object> initConfigUpdate() {
                    HashMap<String, Object> config = new HashMap<>();
                    getNodeAddress();
                    config.put("id2203.project.address", selfAdr);
                    //config.put("id2203.project.bootstrap-address", "127.0.0.1:45678");
                    return config;
                }

                @Override
                public String toString() {
                    return "StartMaster<" + selfAdr.toString() + ">";
                }
            };
        }
    };

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

}
