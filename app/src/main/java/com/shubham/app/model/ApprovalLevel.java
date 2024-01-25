package com.shubham.app.model;

public enum ApprovalLevel {
    APPROVED(0), NEW(1), DISCARD(2), EDIT(3);

    private final int level;

    public int getLevel() {
        return level;
    }

    private ApprovalLevel(int level) {
        this.level = level;
    }

    public ApprovalLevel findByValue(int level) {
        for (ApprovalLevel difficulty : values()) {
            if (difficulty.getLevel() == level) {
                return difficulty;
            }
        }
        System.out.println("Enum Declaration Error ! : Incorrect difficulty, Not supported  : " + level);
        return null;
    }
}
