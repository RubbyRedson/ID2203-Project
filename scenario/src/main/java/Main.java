import org.apache.commons.cli.*;
import scnarios.Setup;
import se.kth.id2203.HostComponent;
import se.kth.id2203.networking.NetAddress;
import se.kth.id2203.networking.NetAddressConverter;
import se.sics.kompics.Kompics;
import se.sics.kompics.config.Config;
import se.sics.kompics.config.ConfigUpdate;
import se.sics.kompics.config.Conversions;
import se.sics.kompics.config.ValueMerger;
import se.sics.kompics.simulator.SimulationScenario;
import se.sics.kompics.simulator.run.LauncherComp;

import java.io.IOException;
import java.io.Serializable;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.UnknownHostException;
import java.util.UUID;

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
