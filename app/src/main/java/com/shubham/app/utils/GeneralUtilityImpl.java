package com.shubham.app.utils;

import org.springframework.stereotype.Service;

import java.util.Objects;

import static com.shubham.app.controller.QuizController.ZERO_LENGTH_STRING;

@Service
public class GeneralUtilityImpl implements GeneralUtility {

    @Override
    public boolean isNullOrEmpty(String s) {
        if (s == null) {
            return true;
        }
        if (Objects.equals(s, ZERO_LENGTH_STRING)) {
            return true;
        }
        return false;
    }

    @Override
    public boolean isNullOrEmpty(Long s) {
        if (s == null) {
            return true;
        }
        if (Objects.equals(s, 0l)) {
            return true;
        }
        return false;
    }
}
