package com.shubham.app.model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public enum QuestionCategory {
    GENERAL(0),
    HISTORY(1),
    FINANCE(2),
    SPORTS(3),
    SCIENCE_AND_TECHNOLOGY(4),
    ENGINEERING(5),
    ENTERTAINMENT(6),
    GEOGRAPHY(7),
    LITERATURE(8),
    FOOD_AND_CUISINE(9),
    NATURE_AND_WILDLIFE(10),
    MYTHOLOGY_AND_RELIGION(11),
    POLITICS(12),
    MUSIC(13);

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
