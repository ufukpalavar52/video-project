package com.videoprocessor.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class StrUtilsTest {

    @Test
    public void UUID_Test() {
        String uuid = StrUtils.UUID();
        assertEquals(5, uuid.split("-").length);
    }

    @Test
    public void truncateString_Test() {
        String str = "Test String";
        assertEquals("Test ", StrUtils.truncateString(str, 5));
        assertNull(StrUtils.truncateString(null, 5));
        assertEquals(str, StrUtils.truncateString(str, 20));
    }
}
