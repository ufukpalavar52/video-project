package com.videoprocessor.service.impl;

import com.videoprocessor.property.VideoProperties;
import com.videoprocessor.service.intf.StorageService;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class VideoSaveStorageFactoryTest {

    @Test
    void makeStorageService_WhenPathTypeExists_ShouldReturnMatchingService() {
        VideoProperties videoProperties = mock(VideoProperties.class);

        StorageService fileStorage = mock(StorageService.class);
        StorageService s3Storage = mock(StorageService.class);

        Map<String, StorageService> storageServiceMap = new HashMap<>();
        storageServiceMap.put("file", fileStorage);
        storageServiceMap.put("s3", s3Storage);

        VideoSaveStorageFactory factory = new VideoSaveStorageFactory(videoProperties, storageServiceMap);

        StorageService result = factory.makeStorageService("s3");

        assertSame(s3Storage, result);
    }

    @Test
    void makeStorageService_WhenPathTypeNotExists_ShouldFallbackToFileService() {
        VideoProperties videoProperties = mock(VideoProperties.class);

        StorageService fileStorage = mock(StorageService.class);

        Map<String, StorageService> storageServiceMap = new HashMap<>();
        storageServiceMap.put("file", fileStorage);

        VideoSaveStorageFactory factory = new VideoSaveStorageFactory(videoProperties, storageServiceMap);

        StorageService result = factory.makeStorageService("unknown");

        assertSame(fileStorage, result);
    }

    @Test
    void makeStorageService_NoArg_ShouldUseVideoPropertiesPathType() {
        VideoProperties videoProperties = mock(VideoProperties.class);
        when(videoProperties.getPathType()).thenReturn("s3");

        StorageService fileStorage = mock(StorageService.class);
        StorageService s3Storage = mock(StorageService.class);

        Map<String, StorageService> storageServiceMap = new HashMap<>();
        storageServiceMap.put("file", fileStorage);
        storageServiceMap.put("s3", s3Storage);

        VideoSaveStorageFactory factory = new VideoSaveStorageFactory(videoProperties, storageServiceMap);

        StorageService result = factory.makeStorageService();

        assertSame(s3Storage, result);
        verify(videoProperties).getPathType();
    }

    @Test
    void makeStorageService_WhenNoMatchAndNoFileFallback_ShouldReturnNull() {
        VideoProperties videoProperties = mock(VideoProperties.class);

        Map<String, StorageService> storageServiceMap = new HashMap<>();
        VideoSaveStorageFactory factory = new VideoSaveStorageFactory(videoProperties, storageServiceMap);

        StorageService result = factory.makeStorageService("unknown");

        assertNull(result);
    }
}
