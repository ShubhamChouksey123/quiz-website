package com.shubham.app.utils;

import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.util.List;
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
    public boolean isNullOrEmpty(Integer s) {
        if (s == null) {
            return true;
        }
        if (Objects.equals(s, 0)) {
            return true;
        }
        return false;
    }

    @Override
    public boolean isNullOrEmpty(BigInteger s) {
        if (s == null) {
            return true;
        }
        if (Objects.equals(s, BigInteger.ZERO)) {
            return true;
        }
        return false;
    }

    @Override
    public boolean isNullOrEmpty(List<?> list) {
        if (list == null) {
            return true;
        }
        if (list.isEmpty()) {
            return true;
        }
        return false;
    }

    @Override
    public String getFormattedName(String name) {

        if (name == null) {
            return null;
        }

        name = name.trim();
        name = name.toLowerCase();
        return name;
    }
}
