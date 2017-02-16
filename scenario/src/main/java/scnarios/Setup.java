package scnarios;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;

import javafx.scene.Parent;
import se.kth.id2203.ParentComponent;
import se.sics.kompics.Init;
import se.sics.kompics.network.Address;
import se.sics.kompics.simulator.SimulationScenario;
import se.sics.kompics.simulator.adaptor.Operation1;
import se.sics.kompics.simulator.adaptor.Operation2;
import se.sics.kompics.simulator.adaptor.distributions.extra.BasicIntSequentialDistribution;
import se.sics.kompics.simulator.events.system.StartNodeEvent;
import se.kth.id2203.networking.NetAddress;


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

                    selfAdr = new NetAddress(ad, 9090);
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
                public String toString() {
                    return "StartMaster<" + selfAdr.toString() + ">";
                }
            };
        }
    };

}
