package com.renatorudolf.simulator.domain;

import java.util.ArrayDeque;
import java.util.Deque;

public final class Machine {

    private final String id;
    private final String name;
    private final long processingTimeMs;
    private final int capacity;

    private final Deque<Item> queue = new ArrayDeque<>();
    private int inProgress;
    private long totalBusyTimeMs;
    private long lastStateChangeMs;
    private int peakQueueSize;
    private int processedCount;

    public Machine(String id, String name, long processingTimeMs, int capacity) {
        if (processingTimeMs < 0) {
            throw new IllegalArgumentException("processingTimeMs must be >= 0");
        }
        if (capacity < 1) {
            throw new IllegalArgumentException("capacity must be >= 1");
        }
        this.id = id;
        this.name = name;
        this.processingTimeMs = processingTimeMs;
        this.capacity = capacity;
    }

    public String id() {
        return id;
    }

    public String name() {
        return name;
    }

    public long processingTimeMs() {
        return processingTimeMs;
    }

    public int capacity() {
        return capacity;
    }

    public boolean hasFreeSlot() {
        return inProgress < capacity;
    }

    public void enqueue(Item item, long now) {
        queue.addLast(item);
        if (queue.size() > peakQueueSize) {
            peakQueueSize = queue.size();
        }
    }

    public Item pollQueue() {
        return queue.pollFirst();
    }

    public int queueSize() {
        return queue.size();
    }

    public void beginProcessing(long now) {
        if (inProgress == 0) {
            lastStateChangeMs = now;
        }
        inProgress++;
    }

    public void finishProcessing(long now) {
        if (inProgress == 0) {
            throw new IllegalStateException("Machine " + id + " has no item in progress");
        }
        inProgress--;
        processedCount++;
        if (inProgress == 0) {
            totalBusyTimeMs += (now - lastStateChangeMs);
        }
    }

    public long totalBusyTimeMs() {
        return totalBusyTimeMs;
    }

    public int peakQueueSize() {
        return peakQueueSize;
    }

    public int processedCount() {
        return processedCount;
    }

    public double utilization(long totalSimulationMs) {
        if (totalSimulationMs <= 0) {
            return 0.0;
        }
        return (double) totalBusyTimeMs / totalSimulationMs;
    }
}
