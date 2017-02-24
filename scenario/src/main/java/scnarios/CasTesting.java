package scnarios;

import se.kth.id2203.ParentComponent;
import se.kth.id2203.kvstore.ClientServiceWithCasTesting;
import se.sics.kompics.simulator.SimulationScenario;
import se.sics.kompics.simulator.adaptor.Operation;
import se.sics.kompics.simulator.events.system.StartNodeEvent;

/**
 * Created by Nick on 2/24/2017.
 */
public class CasTesting {

    public static SimulationScenario casSetup() {
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
                setupClient.startAfterStartOf(3000, setupSlaves);
                terminateAfterTerminationOf(30000, setupClient);
            }
        };

        return scen;
    }

    public static Operation startClient = new Operation<StartNodeEvent>() {
        @Override
        public StartNodeEvent generate() {
            System.out.println("---- startClient with CAS test ----");
            return new StartNode(ClientServiceWithCasTesting.class);
        }
    };

    public static Operation startSlave = new Operation<StartNodeEvent>() {
        @Override
        public StartNodeEvent generate() {
            System.out.println("---- startSlave ----");
            return new StartNode(ParentComponent.class);
        }
    };

    public static Operation startMaster = new Operation<StartNodeEvent>() {

        @Override
        public StartNodeEvent generate() {
            System.out.println("---- startMaster ----");
            return new StartNode(ParentComponent.class, "127.0.0.1", StartNode.MASTER_DEFAULT_PORT, -1);
        }
    };
}
