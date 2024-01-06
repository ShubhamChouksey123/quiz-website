package com.shubham.app.model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public enum QuestionCategory {
    GENERAL(0), HISTORY(1), FINANCE(2), SPORTS(3), SCIENCE_AND_TECHNOLOGY(4), ENGINEERING(5);

    private static final Logger logger = LoggerFactory.getLogger(QuestionCategory.class);

    private final int level;

    private QuestionCategory(int level) {
        this.level = level;
    }

    public int getLevel() {
        return level;
    }

    public QuestionCategory findByValue(int level) {
        for (QuestionCategory difficulty : values()) {
            if (difficulty.getLevel() == level) {
                return difficulty;
            }
        }
        logger.info("Enum Declaration Error ! : Incorrect question category, Not supported  : " + level);
        return null;
    }
}
