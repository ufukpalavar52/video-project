package com.videoprocessor.constant;

import java.util.Arrays;

public enum GifVideoStatus {
    PENDING,
    IN_PROGRESS,
    SUCCESS,
    ERROR,;

    public static GifVideoStatus fromValue(String status) {
        return Arrays.stream(values()).filter(v -> v.name().equalsIgnoreCase(status)).findFirst().orElse(null);
    }
}
