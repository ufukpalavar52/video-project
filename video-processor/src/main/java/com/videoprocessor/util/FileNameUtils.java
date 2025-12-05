package com.videoprocessor.util;


import org.apache.commons.lang3.StringUtils;

import java.util.UUID;

public class FileNameUtils {
    public static String GenerateUUIDFileName(String extension) {
        UUID randomUuid = UUID.randomUUID();
        if (StringUtils.isEmpty(extension)) {
            return randomUuid.toString();
        }
        return randomUuid.toString() + "." + extension;
    }
}
