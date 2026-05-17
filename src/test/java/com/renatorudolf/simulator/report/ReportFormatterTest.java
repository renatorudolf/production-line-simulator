package com.renatorudolf.simulator.report;

import com.renatorudolf.simulator.config.SimulationConfig;
import com.renatorudolf.simulator.config.SimulationConfig.ItemsConfig;
import com.renatorudolf.simulator.config.SimulationConfig.MachineConfig;
import com.renatorudolf.simulator.engine.SimulationResult;
import com.renatorudolf.simulator.engine.Simulator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ReportFormatterTest {

    @Test
    @DisplayName("report contains summary, machine table, and bottleneck line")
    void reportRendersAllSections() {
        SimulationConfig config = new SimulationConfig(
                "Demo",
                List.of(
                        new MachineConfig("fast", "Fast", 50, 1),
                        new MachineConfig("slow", "Slow", 200, 1)
                ),
                List.of("fast", "slow"),
                new ItemsConfig(3, 50)
        );
        SimulationResult result = new Simulator(config).run();

        String report = new ReportFormatter().format(result);

        assertThat(report)
                .contains("Production Line Report — Demo")
                .contains("Total simulated time")
                .contains("Throughput")
                .contains("Machine Statistics")
                .contains("Fast")
                .contains("Slow")
                .contains("Bottleneck: Slow");
    }
}
