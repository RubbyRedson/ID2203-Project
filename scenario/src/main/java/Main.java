import scnarios.CrashAndRestart;
import scnarios.CrashNodes;
import scnarios.Setup;
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
        SimulationScenario bootWithFailure = CrashNodes.simpleSetup();
        bootWithFailure.simulate(LauncherComp.class);

        //SimulationScenario crashAndRestart = CrashAndRestart.simpleSetup();
        //crashAndRestart.simulate(LauncherComp.class);

    }
}
