package com.smarttaskscheduler.model;

public enum Priority {
    HIGH(3),
    MEDIUM(2),
    LOW(1);

    private final int level;

    Priority(int level) {
        this.level = level;
    }

    public int getLevel() {
        return level;
    }
}
