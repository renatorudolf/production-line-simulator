package com.renatorudolf.simulator.engine;

import com.renatorudolf.simulator.config.SimulationConfig;
import com.renatorudolf.simulator.config.SimulationConfig.ItemsConfig;
import com.renatorudolf.simulator.config.SimulationConfig.MachineConfig;
import com.renatorudolf.simulator.domain.Machine;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SimulatorTest {

    @Test
    @DisplayName("single machine processes one item in exactly its processing time")
    void singleMachineSingleItem() {
        SimulationConfig config = new SimulationConfig(
                "trivial",
                List.of(new MachineConfig("only", "Only", 100, 1)),
                List.of("only"),
                new ItemsConfig(1, 0)
        );

        SimulationResult result = new Simulator(config).run();

        assertThat(result.totalSimulatedMs()).isEqualTo(100);
        assertThat(result.items()).singleElement()
                .satisfies(i -> {
                    assertThat(i.isCompleted()).isTrue();
                    assertThat(i.completedAtMs()).isEqualTo(100);
                });
    }

    @Test
    @DisplayName("two machines in series sum their processing times")
    void linearTwoStageFlow() {
        SimulationConfig config = new SimulationConfig(
                "two-stage",
                List.of(
                        new MachineConfig("a", "A", 100, 1),
                        new MachineConfig("b", "B", 200, 1)
                ),
                List.of("a", "b"),
                new ItemsConfig(1, 0)
        );

        SimulationResult result = new Simulator(config).run();

        assertThat(result.totalSimulatedMs()).isEqualTo(300);
        assertThat(result.items()).singleElement()
                .satisfies(i -> assertThat(i.totalTimeInSystemMs()).isEqualTo(300));
    }

    @Test
    @DisplayName("a slow machine creates a queue and becomes the bottleneck")
    void slowMachineCausesQueueing() {
        SimulationConfig config = new SimulationConfig(
                "bottleneck",
                List.of(
                        new MachineConfig("fast", "Fast", 50, 1),
                        new MachineConfig("slow", "Slow", 500, 1)
                ),
                List.of("fast", "slow"),
                new ItemsConfig(5, 50)
        );

        SimulationResult result = new Simulator(config).run();

        Machine slow = result.machines().stream()
                .filter(m -> m.id().equals("slow"))
                .findFirst()
                .orElseThrow();
        Machine fast = result.machines().stream()
                .filter(m -> m.id().equals("fast"))
                .findFirst()
                .orElseThrow();

        assertThat(slow.peakQueueSize()).isGreaterThan(0);
        assertThat(slow.utilization(result.totalSimulatedMs()))
                .isGreaterThan(fast.utilization(result.totalSimulatedMs()));
        assertThat(result.items()).allMatch(i -> i.isCompleted());
    }

    @Test
    @DisplayName("a machine with capacity > 1 processes items in parallel")
    void parallelCapacityReducesTotalTime() {
        SimulationConfig serial = new SimulationConfig(
                "serial",
                List.of(new MachineConfig("only", "Only", 100, 1)),
                List.of("only"),
                new ItemsConfig(4, 0)
        );
        SimulationConfig parallel = new SimulationConfig(
                "parallel",
                List.of(new MachineConfig("only", "Only", 100, 4)),
                List.of("only"),
                new ItemsConfig(4, 0)
        );

        long serialMs = new Simulator(serial).run().totalSimulatedMs();
        long parallelMs = new Simulator(parallel).run().totalSimulatedMs();

        assertThat(serialMs).isEqualTo(400);
        assertThat(parallelMs).isEqualTo(100);
    }
}
