package com.shubham.app.model;

public enum DifficultyLevel {
    LOW(0), MEDIUM(1), HIGH(2);

    private final int level;

    public int getLevel() {
        return level;
    }

    private DifficultyLevel(int level) {
        this.level = level;
    }

    public DifficultyLevel findByValue(int level) {
        for (DifficultyLevel difficulty : values()) {
            if (difficulty.getLevel() == level) {
                return difficulty;
            }
        }
        System.out.println("Enum Declaration Error ! : Incorrect difficulty, Not supported  : " + level);
        return null;
    }
}
