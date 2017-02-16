package scnarios;

import se.kth.id2203.ParentComponent;
import se.kth.id2203.kvstore.ClientService;
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
                        raise(10, startSlave);
                    }
                };

                SimulationScenario.StochasticProcess setupClient = new SimulationScenario.StochasticProcess() {
                    {
                        //10 slaves
                        eventInterArrivalTime(constant(200));
                        raise(1, startClient);
                    }
                };

                setupMaster.start();
                setupSlaves.startAfterStartOf(2000, setupMaster);
                //setupClient.startAfterStartOf(500, setupSlaves);
                terminateAfterTerminationOf(30000, setupSlaves);
            }
        };

        return scen;
    }

    public static Operation startClient = new Operation<StartNodeEvent>() {
        @Override
        public StartNodeEvent generate() {
            return new StartNode(ClientService.class);
        }
    };

    public static Operation startSlave = new Operation<StartNodeEvent>() {
        @Override
        public StartNodeEvent generate() {
            return new StartNode(ParentComponent.class);
        }
    };

    public static Operation startMaster = new Operation<StartNodeEvent>() {

        @Override
        public StartNodeEvent generate() {
            return new StartNode(ParentComponent.class, "127.0.0.1", StartNode.MASTER_DEFAULT_PORT, -1);
        }
    };

}
