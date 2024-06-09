package com.shubham.app.utils;

import java.math.BigInteger;
import java.util.List;

public interface GeneralUtility {
    boolean isNullOrEmpty(String s);

    boolean isNullOrEmpty(Integer s);

    boolean isNullOrEmpty(BigInteger s);

    boolean isNullOrEmpty(List<?> list);

    String getFormattedName(String name);
}
