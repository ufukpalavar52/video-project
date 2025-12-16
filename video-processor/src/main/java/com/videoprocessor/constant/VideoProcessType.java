package com.videoprocessor.constant;

import java.util.Arrays;

public enum VideoProcessType {
    GIF,
    CUT,
    VIDEO,;

    public static VideoProcessType fromValue(String status) {
        return Arrays.stream(values()).filter(v -> v.name().equalsIgnoreCase(status)).findFirst().orElse(null);
    }
}
