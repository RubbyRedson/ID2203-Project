import scnarios.*;
import se.sics.kompics.simulator.SimulationScenario;
import se.sics.kompics.simulator.run.LauncherComp;

/**
 * Created by victoraxelsson on 2017-02-16.
 */
public class Main {
    public static void main(String[] args) {

        long seed = 123;
        SimulationScenario.setSeed(seed);

        //Boot without failures
        //SimulationScenario simpleBootScenario = Setup.simpleSetup();
        //simpleBootScenario.simulate(LauncherComp.class);

        //Boot with failures
        //SimulationScenario bootWithFailure = CrashNodes.simpleSetup();
        //bootWithFailure.simulate(LauncherComp.class);

        //SimulationScenario crashAndRestart = CrashAndRestart.simpleSetup();
        //crashAndRestart.simulate(LauncherComp.class);

        //CAS test
        //SimulationScenario casTesting = CasTesting.casSetup();
        //casTesting.simulate(LauncherComp.class);

        //Test to add a couple of keys on different clients
        //SimulationScenario simpleBootScenario = Linearizability.simpleSetup();
        //simpleBootScenario.simulate(LauncherComp.class);

        //Test the reconfigurability
        SimulationScenario simpleReconfig = Reconfig.simpleSetup();
        simpleReconfig.simulate(LauncherComp.class);
    }
}
