package com.videoprocessor.service;

import com.videoprocessor.constant.ErrorCode;
import com.videoprocessor.exception.CommonException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
@RequiredArgsConstructor
public class FileSaveService implements StorageService {

    @Override
    public Boolean saveFile(String fullPath, byte[] data) throws IOException {
        Path path = Paths.get(fullPath);

        if (Files.notExists(path.getParent())) {
            Files.createDirectories(path.getParent());
        }

        Files.write(path, data);
        return true;
    }

    @Override
    public byte[] getFile(String fullPath) throws IOException {
        Path path = Paths.get(fullPath);
        if (!Files.exists(path)) {
            throw new CommonException(ErrorCode.FILE_NOT_FOUND);
        }
        return Files.readAllBytes(path);
    }

    @Override
    public void deleteFile(String fullPath) throws IOException {
        Path path = Paths.get(fullPath);
        if (Files.notExists(path)) {
            throw new CommonException(ErrorCode.FILE_NOT_FOUND);
        }
        Files.delete(path);
    }
}
