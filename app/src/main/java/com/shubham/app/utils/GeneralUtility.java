package com.shubham.app.utils;

import java.util.List;

public interface GeneralUtility {
    boolean isNullOrEmpty(String s);

    boolean isNullOrEmpty(Integer s);

    boolean isNullOrEmpty(List<?> list);
}
