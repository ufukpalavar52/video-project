package com.videoprocessor.service.impl;

import com.videoprocessor.constant.ErrorCode;
import com.videoprocessor.constant.VideoProcessType;
import com.videoprocessor.constant.VideoStatus;
import com.videoprocessor.exception.CommonException;
import com.videoprocessor.model.entity.Video;
import com.videoprocessor.model.request.VideoRequest;
import com.videoprocessor.property.KafkaProperties;
import com.videoprocessor.property.VideoProperties;
import com.videoprocessor.repository.VideoRepository;
import com.videoprocessor.service.intf.KafkaProducerService;
import com.videoprocessor.service.intf.StorageService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.io.IOException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VideoServiceImplTest {

    @Mock
    private VideoRepository videoRepository;
    @Mock
    private VideoProperties videoProperties;
    @Mock
    private VideoSaveStorageFactory videoSaveStorageFactory;
    @Mock
    private KafkaProducerService kafkaProducer;
    @Mock
    private KafkaProperties kafkaProperties;
    @Mock
    private StorageService storageService;

    @InjectMocks
    private VideoServiceImpl videoService;

    @Test
    void saveWithFile_ShouldPersistAndSendKafka() throws Exception {
        when(videoProperties.getPathType()).thenReturn("file");
        when(videoProperties.getFilePath()).thenReturn("/tmp/videos");
        when(kafkaProperties.getVideoTopic()).thenReturn("video-topic");
        when(videoSaveStorageFactory.makeStorageService()).thenReturn(storageService);
        when(storageService.saveFile(anyString(), any())).thenReturn(true);
        when(videoRepository.save(any(Video.class))).thenAnswer(inv -> inv.getArgument(0));

        VideoRequest request = VideoRequest.builder()
                .startTime(0)
                .endTime(10)
                .processType(VideoProcessType.GIF)
                .build();

        MockMultipartFile file = new MockMultipartFile(
                "file", "sample.mp4", "video/mp4", "dummy-data".getBytes()
        );

        Video result = videoService.save(request, file);

        ArgumentCaptor<Video> captor = ArgumentCaptor.forClass(Video.class);
        verify(videoRepository).save(captor.capture());
        Video saved = captor.getValue();

        assertNotNull(result);
        assertNotNull(saved.getTransactionId());
        assertEquals(0, saved.getStartTime());
        assertEquals(10, saved.getEndTime());
        assertEquals("GIF", saved.getProcessType());
        assertEquals("file", saved.getPathType());
        assertEquals(VideoStatus.IN_PROGRESS.name(), saved.getStatus());
        assertFalse(saved.getIsUrl());
        assertTrue(saved.getPath().startsWith("/tmp/videos/"));
        assertTrue(saved.getPath().endsWith(".mp4"));

        verify(kafkaProducer).sendMessage(eq("video-topic"), any(Video.class));
    }

    @Test
    void saveWithFile_WhenStorageFails_ShouldThrowIOException() throws Exception {
        when(videoProperties.getFilePath()).thenReturn("/tmp/videos");
        when(videoSaveStorageFactory.makeStorageService()).thenReturn(storageService);
        when(storageService.saveFile(anyString(), any())).thenReturn(false);

        VideoRequest request = VideoRequest.builder()
                .startTime(0)
                .endTime(10)
                .processType(VideoProcessType.CUT)
                .build();

        MockMultipartFile file = new MockMultipartFile(
                "file", "sample.mp4", "video/mp4", "dummy-data".getBytes()
        );

        IOException ex = assertThrows(IOException.class, () -> videoService.save(request, file));
        assertEquals("Failed to save file", ex.getMessage());

        verify(videoRepository, never()).save(any());
        verify(kafkaProducer, never()).sendMessage(anyString(), any(Video.class));
    }


    @Test
    void saveUrlRequest_ShouldPersistAndSendKafka() throws Exception {
        when(videoProperties.getPathType()).thenReturn("file");
        when(kafkaProperties.getVideoTopic()).thenReturn("video-topic");
        when(videoRepository.save(any(Video.class))).thenAnswer(inv -> inv.getArgument(0));

        VideoRequest request = VideoRequest.builder()
                .url("https://example.com/video.mp4")
                .startTime(1)
                .endTime(5)
                .status("SUCCESS")
                .processType(VideoProcessType.GIF)
                .build();

        Video result = videoService.save(request);

        ArgumentCaptor<Video> captor = ArgumentCaptor.forClass(Video.class);
        verify(videoRepository).save(captor.capture());
        Video saved = captor.getValue();

        assertNotNull(result);
        assertEquals("https://example.com/video.mp4", saved.getPath());
        assertTrue(saved.getIsUrl());
        assertEquals(VideoStatus.SUCCESS.name(), saved.getStatus());

        verify(kafkaProducer).sendMessage(eq("video-topic"), any(Video.class));
    }

    @Test
    void getVideoTransactionId_WhenExists_ShouldReturnVideo() {
        String tx = "tx-123";
        Video video = new Video();
        video.setTransactionId(tx);

        when(videoRepository.getByTransactionId(tx)).thenReturn(Optional.of(video));

        Video result = videoService.getVideoTransactionId(tx);

        assertEquals(tx, result.getTransactionId());
    }

    @Test
    void getVideoTransactionId_WhenNotExists_ShouldThrowCommonException() {
        String tx = "missing-tx";
        when(videoRepository.getByTransactionId(tx)).thenReturn(Optional.empty());

        CommonException ex = assertThrows(CommonException.class, () -> videoService.getVideoTransactionId(tx));

        assertEquals(ErrorCode.TRANSACTION_NOT_FOUND, ex.getErrorCode());
    }

    @Test
    void getFile_ShouldDelegateToStorageService() throws Exception {
        when(videoSaveStorageFactory.makeStorageService()).thenReturn(storageService);

        byte[] expected = "file-bytes".getBytes();
        when(storageService.getFile("/tmp/out.mp4")).thenReturn(expected);

        byte[] result = videoService.getFile("/tmp/out.mp4");

        assertArrayEquals(expected, result);
        verify(storageService).getFile("/tmp/out.mp4");
    }
}
