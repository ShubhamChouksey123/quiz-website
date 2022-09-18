package com.shubham.app.model;

public enum Difficulty {
    LOW(0), MEDIUM(1), HIGH(2);

    private final int level;

    public int getLevel() {
        return level;
    }

    private Difficulty(int level) {
        this.level = level;
    }

    public Difficulty findByValue(int level) {
        for (Difficulty difficulty : values()) {
            if (difficulty.getLevel() == level) {
                return difficulty;
            }
        }
        System.out.println("Enum Declaration Error ! : Incorrect difficulty, Not supported  : " + level);
        return null;
    }
}
