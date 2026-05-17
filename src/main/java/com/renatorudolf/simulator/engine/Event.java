package com.renatorudolf.simulator.engine;

import com.renatorudolf.simulator.domain.Item;

public final class Event implements Comparable<Event> {

    private final long timestamp;
    private final long sequence;
    private final EventType type;
    private final Item item;
    private final int stageIndex;

    public Event(long timestamp, long sequence, EventType type, Item item, int stageIndex) {
        this.timestamp = timestamp;
        this.sequence = sequence;
        this.type = type;
        this.item = item;
        this.stageIndex = stageIndex;
    }

    public long timestamp() {
        return timestamp;
    }

    public EventType type() {
        return type;
    }

    public Item item() {
        return item;
    }

    public int stageIndex() {
        return stageIndex;
    }

    @Override
    public int compareTo(Event other) {
        int c = Long.compare(timestamp, other.timestamp);
        if (c != 0) {
            return c;
        }
        return Long.compare(sequence, other.sequence);
    }
}
