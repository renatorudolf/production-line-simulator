# Production Line Simulator

A command-line **discrete-event simulator** for production lines, written in **Java 21**. Reads a YAML configuration, simulates items moving through a chain of machines, and produces a metrics report (throughput, utilization, bottleneck, queue depth).

This project is a focused proof-of-concept showcasing senior backend engineering practices: clean separation of concerns, deterministic simulation, and a strong automated test suite.

## Architecture

The codebase is split by responsibility, keeping the simulation core free of I/O concerns:

```
com.renatorudolf.simulator
├── cli         → Picocli command (entry point, argument parsing)
├── config      → YAML loading + immutable configuration records
├── domain      → Item, Machine (runtime state and per-machine metrics)
├── engine      → Discrete-event Simulator, Event, EventType
└── report      → Human-readable report formatting
```

### Why discrete-event simulation?

Rather than spinning up one thread per machine, the engine maintains a virtual clock and a priority queue of events (`ITEM_ARRIVAL`, `PROCESSING_DONE`). This is the same approach used by industrial-grade simulators such as SimPy and AnyLogic. Benefits:

* **Deterministic** — same input always yields the same output (trivial to test and reproduce bugs).
* **Fast** — simulating 10 000 items takes milliseconds; no thread scheduling overhead.
* **Scalable** — adding new event types (failures, maintenance windows, batch operations) is a localized change.

## Quick start

### Requirements
* Java 21+
* The bundled Maven wrapper (no system Maven required)

### Build
```bash
./mvnw package
```
Produces a runnable fat jar at `target/simulator.jar`.

### Run the bundled example
```bash
java -jar target/simulator.jar --config config/example-line.yaml
```

Sample output:
```
================================================================
  Production Line Report — Widget Assembly Line
================================================================

  Total simulated time : 8200 ms
  Items completed      : 25 / 25
  Throughput           : 3.05 items/sec
  Avg time in system   : 3400.0 ms

  Machine Statistics
  ----------------------------------------------------------------
  Machine               Processed    Busy (ms)  Utilization Peak Queue
  ----------------------------------------------------------------
  Cutting Station              25         5000        61.0%         13
  Assembly Station             25         6500        79.3%          5
  Packaging Station            25         7500        91.5%          4

  Bottleneck: Packaging Station (91.5% utilization, peak queue 4)
================================================================
```

### CLI options
| Option | Description |
|---|---|
| `-c`, `--config <path>` | Path to the YAML configuration file (required). |
| `-v`, `--verbose` | Print every event as it is processed (useful for debugging). |
| `-h`, `--help` | Show usage. |
| `-V`, `--version` | Show version. |

## Configuration format

```yaml
simulation:
  name: "Widget Assembly Line"

  machines:
    - id: cutter
      name: "Cutting Station"
      processing_time_ms: 200
      capacity: 1                  # how many items the machine processes in parallel

    - id: assembler
      name: "Assembly Station"
      processing_time_ms: 500
      capacity: 2

  flow: [cutter, assembler]        # order in which items visit machines

  items:
    count: 25                      # total items to simulate
    arrival_interval_ms: 100       # time gap between consecutive item arrivals
```

The loader validates the configuration eagerly: invalid processing times, unknown machine ids in the flow, and missing required fields all fail with a clear error message before the simulation starts.

## Testing

```bash
./mvnw test
```

The suite covers the engine (single machine, multi-stage flow, bottleneck behaviour, parallel capacity), YAML loading (happy path + validation failures), and report formatting.

## Roadmap

The codebase was designed to extend cleanly. Likely next steps:

* **Non-linear flows** — routing rules, branching, merges (the event model already supports this; only the `flow` field is currently linear).
* **Stochastic timings** — Poisson arrivals, normally-distributed processing times.
* **Failures and maintenance** — new event types (`MACHINE_FAILED`, `MAINTENANCE_DONE`) without touching existing handlers.
* **Output formats** — JSON or CSV report for downstream tooling.

## License

MIT.
