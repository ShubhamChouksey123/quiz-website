package com.shubham.app.model;

public enum Difficulty {
    LOW(0), MEDIUM(1), HIGH(2);

    private final int level;

    Difficulty(int level) {
        this.level = level;
    }
}
