package com.renatorudolf.simulator.cli;

import com.renatorudolf.simulator.config.ConfigLoader;
import com.renatorudolf.simulator.config.SimulationConfig;
import com.renatorudolf.simulator.engine.Event;
import com.renatorudolf.simulator.engine.SimulationResult;
import com.renatorudolf.simulator.engine.Simulator;
import com.renatorudolf.simulator.report.ReportFormatter;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.nio.file.Path;
import java.util.concurrent.Callable;
import java.util.function.Consumer;

@Command(
        name = "simulator",
        mixinStandardHelpOptions = true,
        version = "production-line-simulator 1.0.0",
        description = "Simulates a production line defined by a YAML configuration file."
)
public final class SimulateCommand implements Callable<Integer> {

    @Option(
            names = {"-c", "--config"},
            required = true,
            description = "Path to the YAML configuration file."
    )
    private Path configPath;

    @Option(
            names = {"-v", "--verbose"},
            description = "Print every simulation event as it is processed."
    )
    private boolean verbose;

    @Override
    public Integer call() {
        try {
            SimulationConfig config = ConfigLoader.loadFromFile(configPath);
            Consumer<Event> trace = verbose ? this::printEvent : null;
            Simulator simulator = new Simulator(config, trace);
            SimulationResult result = simulator.run();
            System.out.println(new ReportFormatter().format(result));
            return 0;
        } catch (IllegalArgumentException ex) {
            System.err.println("Invalid configuration: " + ex.getMessage());
            return 2;
        } catch (Exception ex) {
            System.err.println("Simulation failed: " + ex.getMessage());
            return 1;
        }
    }

    private void printEvent(Event event) {
        System.out.printf("  [t=%6d] %-16s item=%d stage=%d%n",
                event.timestamp(),
                event.type(),
                event.item().id(),
                event.stageIndex());
    }
}
