package scnarios;

import se.kth.id2203.ParentComponent;
import se.sics.kompics.simulator.SimulationScenario;
import se.sics.kompics.simulator.adaptor.Operation1;
import se.sics.kompics.simulator.adaptor.distributions.extra.BasicIntSequentialDistribution;
import se.sics.kompics.simulator.events.system.StartNodeEvent;

import static scnarios.CrashNodes.killNode;
import static scnarios.CrashNodes.startSlaveWithPortCounter;
import static scnarios.Setup.startMaster;

/**
 * Created by victoraxelsson on 2017-02-17.
 */
public class CrashAndRestart {
    private static final int START_PORTS = 45679; //master + 1

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

                        eventInterArrivalTime(constant(200));
                        raise(10, startSlaveWithPortCounter, new BasicIntSequentialDistribution(START_PORTS));
                    }
                };

                SimulationScenario.StochasticProcess crashSomeNode = new SimulationScenario.StochasticProcess(){
                    {
                        eventInterArrivalTime(constant(10000));

                        //Just kill the first one
                        raise(1, killNode, new BasicIntSequentialDistribution(START_PORTS));
                    }
                };

                SimulationScenario.StochasticProcess restartTheCrashedOne = new SimulationScenario.StochasticProcess(){
                    {
                        eventInterArrivalTime(constant(10000));

                        //Just kill the first one
                        raise(1, startSlaveWithPort, new BasicIntSequentialDistribution(START_PORTS));
                    }
                };

                setupMaster.start();
                setupSlaves.startAfterStartOf(2000, setupMaster);
                crashSomeNode.startAfterStartOf(30000, setupSlaves);
                restartTheCrashedOne.startAfterStartOf(120000, crashSomeNode);
                terminateAfterTerminationOf(30000, restartTheCrashedOne);
            }
        };


    }

    public static Operation1 startSlaveWithPort = new Operation1<StartNodeEvent, Integer>() {
        @Override
        public StartNodeEvent generate(Integer integer) {
            System.out.println("---- startSlaveWithPort: "+ (integer) +" ----");
            return new StartNode(ParentComponent.class, integer);
        }
    };

}
