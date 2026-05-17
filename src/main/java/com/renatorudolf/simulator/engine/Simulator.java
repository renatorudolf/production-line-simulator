package com.renatorudolf.simulator.engine;

import com.renatorudolf.simulator.config.SimulationConfig;
import com.renatorudolf.simulator.domain.Item;
import com.renatorudolf.simulator.domain.Machine;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.function.Consumer;

public final class Simulator {

    private final SimulationConfig config;
    private final Map<String, Machine> machinesById;
    private final List<Machine> flowMachines;
    private final List<Item> items = new ArrayList<>();
    private final PriorityQueue<Event> queue = new PriorityQueue<>();
    private final Consumer<Event> traceListener;

    private long clock;
    private long sequence;

    public Simulator(SimulationConfig config) {
        this(config, null);
    }

    public Simulator(SimulationConfig config, Consumer<Event> traceListener) {
        this.config = config;
        this.machinesById = buildMachines(config);
        this.flowMachines = config.flow().stream().map(machinesById::get).toList();
        this.traceListener = traceListener;
    }

    public SimulationResult run() {
        scheduleArrivals();
        while (!queue.isEmpty()) {
            Event event = queue.poll();
            clock = event.timestamp();
            if (traceListener != null) {
                traceListener.accept(event);
            }
            switch (event.type()) {
                case ITEM_ARRIVAL -> handleArrival(event);
                case PROCESSING_DONE -> handleProcessingDone(event);
            }
        }
        return new SimulationResult(
                config.name(),
                clock,
                new ArrayList<>(machinesById.values()),
                config.flow(),
                items
        );
    }

    private void scheduleArrivals() {
        long interval = config.items().arrivalIntervalMs();
        for (int i = 0; i < config.items().count(); i++) {
            long arrivalTime = i * interval;
            Item item = new Item(i + 1, arrivalTime);
            items.add(item);
            schedule(arrivalTime, EventType.ITEM_ARRIVAL, item, 0);
        }
    }

    private void handleArrival(Event event) {
        Machine machine = flowMachines.get(event.stageIndex());
        if (machine.hasFreeSlot()) {
            startProcessing(machine, event.item(), event.stageIndex());
        } else {
            machine.enqueue(event.item(), clock);
        }
    }

    private void handleProcessingDone(Event event) {
        Machine machine = flowMachines.get(event.stageIndex());
        machine.finishProcessing(clock);

        Item item = event.item();
        item.advanceStage();
        if (item.currentStageIndex() >= flowMachines.size()) {
            item.markCompleted(clock);
        } else {
            schedule(clock, EventType.ITEM_ARRIVAL, item, item.currentStageIndex());
        }

        Item next = machine.pollQueue();
        if (next != null) {
            startProcessing(machine, next, event.stageIndex());
        }
    }

    private void startProcessing(Machine machine, Item item, int stageIndex) {
        machine.beginProcessing(clock);
        schedule(clock + machine.processingTimeMs(),
                EventType.PROCESSING_DONE, item, stageIndex);
    }

    private void schedule(long timestamp, EventType type, Item item, int stageIndex) {
        queue.add(new Event(timestamp, ++sequence, type, item, stageIndex));
    }

    private static Map<String, Machine> buildMachines(SimulationConfig config) {
        Map<String, Machine> map = new LinkedHashMap<>();
        for (SimulationConfig.MachineConfig mc : config.machines()) {
            map.put(mc.id(), new Machine(mc.id(), mc.name(), mc.processingTimeMs(), mc.capacity()));
        }
        return map;
    }
}
