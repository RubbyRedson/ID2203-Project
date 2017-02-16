package scnarios;

import se.kth.id2203.ParentComponent;
import se.kth.id2203.networking.NetAddress;
import se.sics.kompics.Init;
import se.sics.kompics.network.Address;
import se.sics.kompics.simulator.SimulationScenario;
import se.sics.kompics.simulator.adaptor.Operation;
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

    public static final int MASTER_PORT = 45678;
    public static final String MASTER_IP = "127.0.0.1";
    public static final String SLAVE_IP = "127.0.0.1";

    public static SimulationScenario simpleSetup() {
        SimulationScenario scen = new SimulationScenario() {
            {
                SimulationScenario.StochasticProcess setupMaster = new SimulationScenario.StochasticProcess() {
                    {
                        //One master
                        eventInterArrivalTime(constant(1000));
                        raise(1, startMaster);
                    }
                };

                SimulationScenario.StochasticProcess setupSlaves = new SimulationScenario.StochasticProcess() {
                    {
                        //10 slaves
                        eventInterArrivalTime(constant(200));
                        raise(100, startSlave);
                    }
                };

                setupMaster.start();
                setupSlaves.startAfterStartOf(500, setupMaster);
                terminateAfterTerminationOf(30000, setupSlaves);
            }
        };

        return scen;
    }


    public static Operation startSlave = new Operation<StartNodeEvent>() {

        @Override
        public StartNodeEvent generate() {
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

                    try {
                        selfAdr = new NetAddress(InetAddress.getByName(SLAVE_IP), getFreePort());
                    } catch (UnknownHostException e) {
                        e.printStackTrace();
                    }

                    return selfAdr;
                }

                @Override
                public Class getComponentDefinition() {
                    return ParentComponent.class;
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
                    config.put("id2203.project.bootstrap-address",  new NetAddress(selfAdr.getIp(), MASTER_PORT));

                    return config;
                }

                @Override
                public String toString() {
                    return "StartSlave<" + selfAdr.toString() + ">";
                }
            };
        }
    };



    public static Operation startMaster = new Operation<StartNodeEvent>() {

        @Override
        public StartNodeEvent generate() {
            return new StartNodeEvent() {

                NetAddress selfAdr;

                @Override
                public Address getNodeAddress() {
                    try {
                        selfAdr = new NetAddress(InetAddress.getByName(MASTER_IP), MASTER_PORT);
                    } catch (UnknownHostException e) {
                        e.printStackTrace();
                    }

                    System.out.println(selfAdr);
                    return selfAdr;
                }

                @Override
                public Class getComponentDefinition() {
                    return ParentComponent.class;
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
