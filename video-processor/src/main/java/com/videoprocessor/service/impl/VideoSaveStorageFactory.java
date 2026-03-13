package com.videoprocessor.service.impl;

import com.videoprocessor.constant.PathType;
import com.videoprocessor.property.VideoProperties;
import com.videoprocessor.service.intf.StorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class VideoSaveStorageFactory {
    private final VideoProperties fileProperties;
    private final Map<String, StorageService> storageServiceMap;

    public StorageService makeStorageService(String pathType) {
        StorageService storageService = storageServiceMap.get(pathType);
        if (storageService == null) {
            return storageServiceMap.get(PathType.FILE.name().toLowerCase());
        }
        return storageService;
    }

    public StorageService makeStorageService() {
        return makeStorageService(fileProperties.getPathType());
    }
}
