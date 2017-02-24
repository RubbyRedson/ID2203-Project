package scnarios;

import se.kth.id2203.ParentComponent;
import se.kth.id2203.kvstore.KeyAdderClient;
import se.sics.kompics.simulator.SimulationScenario;
import se.sics.kompics.simulator.adaptor.Operation;
import se.sics.kompics.simulator.adaptor.Operation1;
import se.sics.kompics.simulator.adaptor.distributions.extra.BasicIntSequentialDistribution;
import se.sics.kompics.simulator.events.system.StartNodeEvent;

import static scnarios.CrashNodes.START_PORTS;
import static scnarios.CrashNodes.killNode;
import static scnarios.CrashNodes.startSlaveWithPortCounter;
import static scnarios.Linearizability.startKeyAdderClient;
import static scnarios.Setup.startClient;
import static scnarios.Setup.startMaster;
import static scnarios.Setup.startSlave;

/**
 * Created by victoraxelsson on 2017-02-24.
 */
public class Reconfig {
    private static int startedCounter = 0;

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
                        startedCounter = 0;

                        eventInterArrivalTime(constant(200));
                        raise(10, startSlaveWithPortCounterLocal, new BasicIntSequentialDistribution(START_PORTS));
                    }
                };

                SimulationScenario.StochasticProcess crashSomeNode = new SimulationScenario.StochasticProcess(){
                    {
                        eventInterArrivalTime(constant(10000));

                        //Just kill the first one
                        raise(1, killNode, new BasicIntSequentialDistribution(START_PORTS));
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

                setupClient.startAfterStartOf(2000, setupSlaves);

                crashSomeNode.startAfterStartOf(30000, setupClient);
                terminateAfterTerminationOf(15000, crashSomeNode);
            }
        };
    }

    private static Operation1 startSlaveWithPortCounterLocal = new Operation1<StartNodeEvent, Integer>() {

        @Override
        public StartNodeEvent generate(Integer integer) {
            startedCounter++;
            System.out.println("---- startSlaveWithPortCounter: "+ (integer + startedCounter -1) +" ----");
            return new StartNode(ParentComponent.class, integer + startedCounter -1);
        }
    };

}
