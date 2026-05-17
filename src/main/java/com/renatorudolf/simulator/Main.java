package com.renatorudolf.simulator;

import com.renatorudolf.simulator.cli.SimulateCommand;
import picocli.CommandLine;

public final class Main {
    public static void main(String[] args) {
        int exitCode = new CommandLine(new SimulateCommand()).execute(args);
        System.exit(exitCode);
    }
}
