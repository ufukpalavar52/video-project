package com.videoprocessor.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.videoprocessor.constant.VideoStatus;
import com.videoprocessor.model.dto.VideoError;
import com.videoprocessor.model.entity.Video;
import com.videoprocessor.model.entity.VideoErrorLog;
import com.videoprocessor.repository.VideoErrorLogRepository;
import com.videoprocessor.repository.VideoRepository;
import com.videoprocessor.service.intf.StorageService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VideoKafkaServiceImplTest {

    @Mock
    private VideoRepository videoRepository;

    @Mock
    private VideoErrorLogRepository errorLogRepository;

    @Mock
    private ObjectMapper mapper;

    @Mock
    private VideoSaveStorageFactory videoSaveStorageFactory;

    @Mock
    private StorageService storageService;

    @InjectMocks
    private VideoKafkaServiceImpl service;

    @Test
    void consumeGifFinish_WhenNotUrl_ShouldSetSuccessSaveAndDeleteSource() throws Exception {
        Video video = new Video();
        video.setTransactionId("tx-1");
        video.setIsUrl(false);
        video.setPath("/tmp/in.mp4");
        video.setPathType("file");

        when(mapper.readValue("msg", Video.class)).thenReturn(video);
        when(videoSaveStorageFactory.makeStorageService("file")).thenReturn(storageService);

        service.consumeGifFinish("msg");

        assertEquals(VideoStatus.SUCCESS.name(), video.getStatus());
        verify(videoRepository).save(video);
        verify(videoSaveStorageFactory).makeStorageService("file");
        verify(storageService).deleteFile("/tmp/in.mp4");
    }

    @Test
    void consumeGifFinish_WhenUrl_ShouldSetSuccessAndSkipDelete() throws Exception {
        Video video = new Video();
        video.setTransactionId("tx-2");
        video.setIsUrl(true);
        video.setPath("/tmp/in.mp4");
        video.setPathType("file");

        when(mapper.readValue("msg", Video.class)).thenReturn(video);

        service.consumeGifFinish("msg");

        assertEquals(VideoStatus.SUCCESS.name(), video.getStatus());
        verify(videoRepository).save(video);
        verifyNoInteractions(videoSaveStorageFactory);
    }

    @Test
    void consumeGifError_WhenTransactionNotFound_ShouldReturnWithoutSaveOrDelete() throws Exception {
        VideoError error = new VideoError();
        error.setTransactionId("missing-tx");
        error.setMessage("any");

        when(mapper.readValue("msg", VideoError.class)).thenReturn(error);
        when(videoRepository.getByTransactionId("missing-tx")).thenReturn(Optional.empty());

        service.consumeGifError("msg");

        verify(videoRepository).getByTransactionId("missing-tx");
        verify(videoRepository, never()).save(any(Video.class));
        verifyNoInteractions(errorLogRepository, videoSaveStorageFactory);
    }

    @Test
    void consumeGifError_WhenNotUrl_ShouldSetErrorSaveLogAndDeleteSource() throws Exception {
        String longMessage = "x".repeat(6000);

        VideoError error = new VideoError();
        error.setTransactionId("tx-3");
        error.setMessage(longMessage);

        Video video = new Video();
        video.setTransactionId("tx-3");
        video.setIsUrl(false);
        video.setPath("/tmp/failed.mp4");
        video.setPathType("file");

        when(mapper.readValue("msg", VideoError.class)).thenReturn(error);
        when(videoRepository.getByTransactionId("tx-3")).thenReturn(Optional.of(video));
        when(videoSaveStorageFactory.makeStorageService("file")).thenReturn(storageService);

        service.consumeGifError("msg");

        assertEquals(VideoStatus.ERROR.name(), video.getStatus());
        verify(videoRepository).save(video);

        ArgumentCaptor<VideoErrorLog> logCaptor = ArgumentCaptor.forClass(VideoErrorLog.class);
        verify(errorLogRepository).save(logCaptor.capture());

        VideoErrorLog savedLog = logCaptor.getValue();
        assertEquals("tx-3", savedLog.getTransactionId());
        assertNotNull(savedLog.getMessage());
        assertEquals(5000, savedLog.getMessage().length());
        assertEquals(longMessage.substring(0, 5000), savedLog.getMessage());

        verify(videoSaveStorageFactory).makeStorageService("file");
        verify(storageService).deleteFile("/tmp/failed.mp4");
    }

    @Test
    void consumeGifError_WhenUrl_ShouldSetErrorSaveLogAndSkipDelete() throws Exception {
        VideoError error = new VideoError();
        error.setTransactionId("tx-4");
        error.setMessage("processing failed");

        Video video = new Video();
        video.setTransactionId("tx-4");
        video.setIsUrl(true);
        video.setPath("/tmp/failed.mp4");
        video.setPathType("file");

        when(mapper.readValue("msg", VideoError.class)).thenReturn(error);
        when(videoRepository.getByTransactionId("tx-4")).thenReturn(Optional.of(video));

        service.consumeGifError("msg");

        assertEquals(VideoStatus.ERROR.name(), video.getStatus());
        verify(videoRepository).save(video);
        verify(errorLogRepository).save(any(VideoErrorLog.class));
        verifyNoInteractions(videoSaveStorageFactory);
    }

    @Test
    void deleteVideo_WhenStorageThrows_ShouldSwallowException() throws Exception {
        when(videoSaveStorageFactory.makeStorageService("file")).thenReturn(storageService);
        doThrow(new RuntimeException("delete failed"))
                .when(storageService).deleteFile("/tmp/fail.mp4");

        assertDoesNotThrow(() -> service.deleteVideo("/tmp/fail.mp4", "file"));

        verify(videoSaveStorageFactory).makeStorageService("file");
        verify(storageService).deleteFile("/tmp/fail.mp4");
    }
}
