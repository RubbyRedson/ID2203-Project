package scnarios;

import se.kth.id2203.client.ParentComponent;
import se.kth.id2203.kvstore.ClientService;
import se.kth.id2203.networking.NetAddress;
import se.sics.kompics.Init;
import se.sics.kompics.KompicsEvent;
import se.sics.kompics.network.Address;
import se.sics.kompics.simulator.SimulationScenario;
import se.sics.kompics.simulator.adaptor.Operation;
import se.sics.kompics.simulator.events.system.StartNodeEvent;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;

import static scnarios.Setup.*;

/**
 * Created by victoraxelsson on 2017-02-16.
 */
public class AddKey {
    public static SimulationScenario addKeys() {
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
                        raise(1, startSlave);
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
                setupSlaves.startAfterStartOf(500, setupMaster);
                setupClient.startAfterStartOf(500, setupSlaves);
                terminateAfterTerminationOf(30000, setupClient);
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

}
