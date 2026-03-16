package com.videoprocessor.util;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class FileNameUtilsTest {

    @Test
    public void GenerateUUIDFileName_ExtNotEmpty() {
        String ext = "mp4";
        String filename = FileNameUtils.GenerateUUIDFileName(ext);

        assertTrue(filename.contains("." + ext));
    }

    @Test
    public void GenerateUUIDFileName_ExtEmpty() {
        String ext = "";
        String filename = FileNameUtils.GenerateUUIDFileName(ext);
        assertFalse(filename.contains("."));
    }

}
