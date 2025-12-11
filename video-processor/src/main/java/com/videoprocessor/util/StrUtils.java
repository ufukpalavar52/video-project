package com.videoprocessor.util;

import java.util.UUID;

public class StrUtils {
    public static String UUID() {
        return UUID.randomUUID().toString();
    }

    public static String truncateString(String inputString, int maxLength) {
        if (inputString == null) {
            return null;
        }
        int endIndex = Math.min(inputString.length(), maxLength);

        return inputString.substring(0, endIndex);
    }
}
