package com.videoprocessor.service;

import com.videoprocessor.constant.PathType;
import com.videoprocessor.property.VideoProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class VideoSaveStorageFactory {
    private final VideoProperties fileProperties;
    private final S3SaveService s3SaveService;
    private final FileSaveService fileSaveService;

    public StorageService makeStorageService(String pathType) {
        switch (PathType.fromValue(pathType)) {
            case S3 -> {
                return s3SaveService;
            }
            default -> {
                return  fileSaveService;
            }
        }
    }

    public StorageService makeStorageService() {
        return makeStorageService(fileProperties.getPathType());
    }
}
