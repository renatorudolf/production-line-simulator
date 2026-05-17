package com.renatorudolf.simulator.domain;

public final class Item {

    private final int id;
    private final long createdAtMs;
    private int currentStageIndex;
    private long completedAtMs = -1L;

    public Item(int id, long createdAtMs) {
        this.id = id;
        this.createdAtMs = createdAtMs;
        this.currentStageIndex = 0;
    }

    public int id() {
        return id;
    }

    public long createdAtMs() {
        return createdAtMs;
    }

    public int currentStageIndex() {
        return currentStageIndex;
    }

    public void advanceStage() {
        currentStageIndex++;
    }

    public void markCompleted(long now) {
        this.completedAtMs = now;
    }

    public boolean isCompleted() {
        return completedAtMs >= 0;
    }

    public long completedAtMs() {
        return completedAtMs;
    }

    public long totalTimeInSystemMs() {
        if (!isCompleted()) {
            throw new IllegalStateException("Item " + id + " has not completed yet");
        }
        return completedAtMs - createdAtMs;
    }
}
