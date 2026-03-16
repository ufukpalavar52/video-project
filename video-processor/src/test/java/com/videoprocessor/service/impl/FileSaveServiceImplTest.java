package com.videoprocessor.service.impl;

import com.videoprocessor.constant.ErrorCode;
import com.videoprocessor.exception.CommonException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class FileSaveServiceImplTest {

    @TempDir
    Path tempDir;

    private final FileSaveServiceImpl fileSaveService = new FileSaveServiceImpl();

    @Test
    void saveFile_ShouldCreateDirectoriesAndWriteData() throws Exception {
        Path target = tempDir.resolve("nested/dir/video.mp4");
        byte[] data = "dummy-video-bytes".getBytes();

        Boolean result = fileSaveService.saveFile(target.toString(), data);

        assertTrue(result);
        assertTrue(Files.exists(target));
        assertArrayEquals(data, Files.readAllBytes(target));
    }

    @Test
    void getFile_WhenFileExists_ShouldReturnBytes() throws Exception {
        Path target = tempDir.resolve("video.mp4");
        byte[] expected = "file-content".getBytes();
        Files.write(target, expected);

        byte[] actual = fileSaveService.getFile(target.toString());

        assertArrayEquals(expected, actual);
    }

    @Test
    void getFile_WhenFileNotExists_ShouldThrowCommonException() {
        Path missing = tempDir.resolve("missing.mp4");

        CommonException ex = assertThrows(
                CommonException.class,
                () -> fileSaveService.getFile(missing.toString())
        );

        assertEquals(ErrorCode.FILE_NOT_FOUND, ex.getErrorCode());
    }

    @Test
    void deleteFile_WhenFileExists_ShouldDeleteFile() throws Exception {
        Path target = tempDir.resolve("to-delete.mp4");
        Files.write(target, "delete-me".getBytes());
        assertTrue(Files.exists(target));

        fileSaveService.deleteFile(target.toString());

        assertFalse(Files.exists(target));
    }

    @Test
    void deleteFile_WhenFileNotExists_ShouldThrowCommonException() {
        Path missing = tempDir.resolve("not-found.mp4");

        CommonException ex = assertThrows(
                CommonException.class,
                () -> fileSaveService.deleteFile(missing.toString())
        );

        assertEquals(ErrorCode.FILE_NOT_FOUND, ex.getErrorCode());
    }
}
