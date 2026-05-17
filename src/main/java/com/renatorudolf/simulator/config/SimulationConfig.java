package com.renatorudolf.simulator.config;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record SimulationConfig(
        String name,
        List<MachineConfig> machines,
        List<String> flow,
        ItemsConfig items
) {

    public SimulationConfig {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("simulation.name is required");
        }
        if (machines == null || machines.isEmpty()) {
            throw new IllegalArgumentException("at least one machine is required");
        }
        if (flow == null || flow.isEmpty()) {
            throw new IllegalArgumentException("flow must list at least one machine id");
        }
        if (items == null) {
            throw new IllegalArgumentException("items section is required");
        }
        List<String> machineIds = machines.stream().map(MachineConfig::id).toList();
        for (String stage : flow) {
            if (!machineIds.contains(stage)) {
                throw new IllegalArgumentException(
                        "flow references unknown machine id: " + stage);
            }
        }
    }

    public record MachineConfig(
            String id,
            String name,
            @JsonProperty("processing_time_ms") long processingTimeMs,
            int capacity
    ) {
        public MachineConfig {
            if (id == null || id.isBlank()) {
                throw new IllegalArgumentException("machine.id is required");
            }
            if (name == null || name.isBlank()) {
                name = id;
            }
            if (capacity < 1) {
                capacity = 1;
            }
            if (processingTimeMs < 0) {
                throw new IllegalArgumentException(
                        "machine.processing_time_ms must be >= 0 for " + id);
            }
        }
    }

    public record ItemsConfig(
            int count,
            @JsonProperty("arrival_interval_ms") long arrivalIntervalMs
    ) {
        public ItemsConfig {
            if (count < 1) {
                throw new IllegalArgumentException("items.count must be >= 1");
            }
            if (arrivalIntervalMs < 0) {
                throw new IllegalArgumentException("items.arrival_interval_ms must be >= 0");
            }
        }
    }
}
