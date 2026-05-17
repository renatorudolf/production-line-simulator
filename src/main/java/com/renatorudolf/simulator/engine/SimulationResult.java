package com.renatorudolf.simulator.engine;

import com.renatorudolf.simulator.domain.Item;
import com.renatorudolf.simulator.domain.Machine;

import java.util.List;

public record SimulationResult(
        String simulationName,
        long totalSimulatedMs,
        List<Machine> machines,
        List<String> flow,
        List<Item> items
) {
}
