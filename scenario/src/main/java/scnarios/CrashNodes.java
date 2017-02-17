package scnarios;

import se.kth.id2203.ParentComponent;
import se.sics.kompics.simulator.SimulationScenario;
import se.sics.kompics.simulator.adaptor.Operation1;
import se.sics.kompics.simulator.adaptor.distributions.extra.BasicIntSequentialDistribution;
import se.sics.kompics.simulator.events.system.KillNodeEvent;
import se.sics.kompics.simulator.events.system.StartNodeEvent;

import static scnarios.Setup.startMaster;


/**
 * Created by victoraxelsson on 2017-02-17.
 */
public class CrashNodes {

    private static int startedCounter = 0;
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
                        startedCounter = 0;

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

                setupMaster.start();
                setupSlaves.startAfterStartOf(2000, setupMaster);
                crashSomeNode.startAfterStartOf(30000, setupSlaves);
                terminateAfterTerminationOf(15000, crashSomeNode);
            }
        };
    }


    public static Operation1 killNode = new Operation1<KillNodeEvent, Integer>() {
        @Override
        public KillNodeEvent generate(Integer integer) {
            System.out.println("---- killNode: "+integer+" ----");
            return new KillNode(integer);
        }
    };

    public static Operation1 startSlaveWithPortCounter = new Operation1<StartNodeEvent, Integer>() {

        @Override
        public StartNodeEvent generate(Integer integer) {
            startedCounter++;
            System.out.println("---- startSlaveWithPortCounter: "+ (integer + startedCounter -1) +" ----");
            return new StartNode(ParentComponent.class, integer + startedCounter -1);
        }
    };

}
