package com.renatorudolf.simulator.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ConfigLoaderTest {

    @Test
    @DisplayName("loads a valid YAML configuration with snake_case fields")
    void loadsValidConfig() throws IOException {
        String yaml = """
                simulation:
                  name: "Test Line"
                  machines:
                    - id: cutter
                      name: Cutter
                      processing_time_ms: 200
                      capacity: 1
                    - id: packer
                      name: Packer
                      processing_time_ms: 300
                      capacity: 2
                  flow: [cutter, packer]
                  items:
                    count: 10
                    arrival_interval_ms: 50
                """;

        SimulationConfig config = ConfigLoader.loadFromStream(toStream(yaml));

        assertThat(config.name()).isEqualTo("Test Line");
        assertThat(config.machines()).hasSize(2);
        assertThat(config.machines().get(0).processingTimeMs()).isEqualTo(200);
        assertThat(config.machines().get(1).capacity()).isEqualTo(2);
        assertThat(config.flow()).containsExactly("cutter", "packer");
        assertThat(config.items().count()).isEqualTo(10);
        assertThat(config.items().arrivalIntervalMs()).isEqualTo(50);
    }

    @Test
    @DisplayName("rejects a flow stage that references an unknown machine id")
    void rejectsUnknownMachineInFlow() {
        String yaml = """
                simulation:
                  name: "Bad Line"
                  machines:
                    - id: only
                      processing_time_ms: 100
                      capacity: 1
                  flow: [only, ghost]
                  items:
                    count: 1
                    arrival_interval_ms: 0
                """;

        assertThatThrownBy(() -> ConfigLoader.loadFromStream(toStream(yaml)))
                .rootCause()
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("ghost");
    }

    private static ByteArrayInputStream toStream(String text) {
        return new ByteArrayInputStream(text.getBytes(StandardCharsets.UTF_8));
    }
}
