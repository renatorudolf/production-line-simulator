package com.renatorudolf.simulator.report;

import com.renatorudolf.simulator.domain.Item;
import com.renatorudolf.simulator.domain.Machine;
import com.renatorudolf.simulator.engine.SimulationResult;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public final class ReportFormatter {

    private static final String DIVIDER =
            "================================================================";

    public String format(SimulationResult result) {
        StringBuilder sb = new StringBuilder();
        sb.append(DIVIDER).append('\n');
        sb.append("  Production Line Report — ").append(result.simulationName()).append('\n');
        sb.append(DIVIDER).append('\n');

        appendSummary(sb, result);
        appendMachineTable(sb, result);
        appendBottleneck(sb, result);

        sb.append(DIVIDER).append('\n');
        return sb.toString();
    }

    private void appendSummary(StringBuilder sb, SimulationResult result) {
        List<Item> completed = result.items().stream().filter(Item::isCompleted).toList();
        int total = result.items().size();
        long totalMs = result.totalSimulatedMs();
        double throughput = totalMs > 0
                ? (completed.size() * 1000.0) / totalMs
                : 0.0;

        double avgTimeInSystem = completed.stream()
                .mapToLong(Item::totalTimeInSystemMs)
                .average()
                .orElse(0.0);

        sb.append('\n');
        sb.append(String.format(Locale.US, "  Total simulated time : %d ms%n", totalMs));
        sb.append(String.format(Locale.US, "  Items completed      : %d / %d%n", completed.size(), total));
        sb.append(String.format(Locale.US, "  Throughput           : %.2f items/sec%n", throughput));
        sb.append(String.format(Locale.US, "  Avg time in system   : %.1f ms%n", avgTimeInSystem));
        sb.append('\n');
    }

    private void appendMachineTable(StringBuilder sb, SimulationResult result) {
        sb.append("  Machine Statistics\n");
        sb.append("  ----------------------------------------------------------------\n");
        sb.append(String.format(Locale.US, "  %-20s %10s %12s %12s %10s%n",
                "Machine", "Processed", "Busy (ms)", "Utilization", "Peak Queue"));
        sb.append("  ----------------------------------------------------------------\n");

        long totalMs = result.totalSimulatedMs();
        for (String stage : result.flow()) {
            Machine m = findMachine(result, stage);
            sb.append(String.format(Locale.US, "  %-20s %10d %12d %11.1f%% %10d%n",
                    m.name(),
                    m.processedCount(),
                    m.totalBusyTimeMs(),
                    m.utilization(totalMs) * 100.0,
                    m.peakQueueSize()));
        }
        sb.append('\n');
    }

    private void appendBottleneck(StringBuilder sb, SimulationResult result) {
        long totalMs = result.totalSimulatedMs();
        if (totalMs == 0) {
            return;
        }
        Machine bottleneck = result.flow().stream()
                .map(stage -> findMachine(result, stage))
                .max(Comparator.comparingDouble(m -> m.utilization(totalMs)))
                .orElseThrow();
        sb.append(String.format(Locale.US, "  Bottleneck: %s (%.1f%% utilization, peak queue %d)%n%n",
                bottleneck.name(),
                bottleneck.utilization(totalMs) * 100.0,
                bottleneck.peakQueueSize()));
    }

    private Machine findMachine(SimulationResult result, String id) {
        return result.machines().stream()
                .filter(m -> m.id().equals(id))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Machine not found: " + id));
    }
}
