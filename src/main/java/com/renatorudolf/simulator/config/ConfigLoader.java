package com.renatorudolf.simulator.config;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

public final class ConfigLoader {

    private static final ObjectMapper MAPPER = new ObjectMapper(new YAMLFactory())
            .registerModule(new ParameterNamesModule())
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    private ConfigLoader() {
    }

    public static SimulationConfig loadFromFile(Path path) throws IOException {
        if (!Files.exists(path)) {
            throw new IOException("Configuration file not found: " + path);
        }
        try (InputStream in = Files.newInputStream(path)) {
            return loadFromStream(in);
        }
    }

    public static SimulationConfig loadFromStream(InputStream in) throws IOException {
        Wrapper wrapper = MAPPER.readValue(in, Wrapper.class);
        if (wrapper == null || wrapper.simulation == null) {
            throw new IOException("Invalid configuration: missing 'simulation' root");
        }
        return wrapper.simulation;
    }

    private static final class Wrapper {
        public SimulationConfig simulation;
    }
}
