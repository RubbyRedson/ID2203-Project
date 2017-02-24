package scnarios;

import se.kth.id2203.kvstore.ClientService;
import se.kth.id2203.kvstore.KeyAdderClient;
import se.sics.kompics.simulator.SimulationScenario;
import se.sics.kompics.simulator.adaptor.Operation;
import se.sics.kompics.simulator.adaptor.distributions.extra.BasicIntSequentialDistribution;
import se.sics.kompics.simulator.events.system.StartNodeEvent;

import static scnarios.Setup.startMaster;
import static scnarios.Setup.startSlave;

/**
 * Created by victoraxelsson on 2017-02-24.
 */
public class Linearizability {

    public static SimulationScenario simpleSetup() {
        return new SimulationScenario() {
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

                SimulationScenario.StochasticProcess setupClients = new SimulationScenario.StochasticProcess() {
                    {
                        //10 slaves
                        eventInterArrivalTime(constant(200));
                        raise(1, startKeyAdderClient);
                    }
                };


                setupMaster.start();
                setupSlaves.startAfterStartOf(2000, setupMaster);
                setupClients.startAfterStartOf(2000, setupSlaves);
                terminateAfterTerminationOf(30000, setupClients);

                // setupMaster.start();
                // setupSlaves.startAfterStartOf(2000, setupMaster);
                //setupClients.startAfterStartOf(3000, setupSlaves);
                //terminateAfterTerminationOf(30000, setupClient);
            }
        };
    }

    public static Operation startKeyAdderClient = new Operation<StartNodeEvent>() {
        @Override
        public StartNodeEvent generate() {
            System.out.println("---- startKeyAdderClient ----");
            return new StartNode(KeyAdderClient.class);
        }
    };
}
