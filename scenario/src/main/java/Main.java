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
        SimulationScenario simpleBootScenario = Setup.simpleSetup();

        simpleBootScenario.simulate(LauncherComp.class);

    }
}
