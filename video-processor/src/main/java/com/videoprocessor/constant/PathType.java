package com.videoprocessor.constant;

import java.util.Arrays;

public enum PathType {
    FILE,
    S3,;

    public static PathType fromValue(String pathType) {
        return Arrays.stream(values()).filter(v -> v.name().equalsIgnoreCase(pathType)).findFirst().orElse(null);
    }
}
