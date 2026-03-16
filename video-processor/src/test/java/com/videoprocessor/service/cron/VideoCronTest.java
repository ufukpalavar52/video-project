package com.videoprocessor.service.cron;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.videoprocessor.constant.VideoStatus;
import com.videoprocessor.model.entity.Video;
import com.videoprocessor.property.KafkaProperties;
import com.videoprocessor.property.VideoProperties;
import com.videoprocessor.repository.VideoRepository;
import com.videoprocessor.service.intf.KafkaProducerService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VideoCronTest {

    @Mock
    private VideoRepository videoRepository;

    @Mock
    private VideoProperties videoProperties;

    @Mock
    private KafkaProducerService kafkaProducer;

    @Mock
    private KafkaProperties kafkaProperties;

    @InjectMocks
    private VideoCron videoCron;

    @Test
    void checkInProgress_WhenNoVideos_ShouldOnlyQueryRepository() {
        int timeoutMinutes = 30;
        when(videoProperties.getInProgressTimeout()).thenReturn(timeoutMinutes);
        when(videoRepository.getByCreatedAtBeforeAndStatus(any(LocalDateTime.class), eq(VideoStatus.IN_PROGRESS.name())))
                .thenReturn(List.of());

        LocalDateTime before = LocalDateTime.now();
        videoCron.checkInProgress();
        LocalDateTime after = LocalDateTime.now();

        ArgumentCaptor<LocalDateTime> dateCaptor = ArgumentCaptor.forClass(LocalDateTime.class);
        verify(videoRepository).getByCreatedAtBeforeAndStatus(dateCaptor.capture(), eq(VideoStatus.IN_PROGRESS.name()));

        LocalDateTime captured = dateCaptor.getValue();
        assertFalse(captured.isBefore(before.plusMinutes(timeoutMinutes)));
        assertFalse(captured.isAfter(after.plusMinutes(timeoutMinutes)));

        verifyNoInteractions(kafkaProducer, kafkaProperties);
    }

    @Test
    void checkInProgress_WhenVideosExist_ShouldPublishEachVideo() throws Exception {
        int timeoutMinutes = 30;
        String topic = "video-topic";

        Video video1 = new Video();
        video1.setTransactionId("tx-1");
        Video video2 = new Video();
        video2.setTransactionId("tx-2");

        when(videoProperties.getInProgressTimeout()).thenReturn(timeoutMinutes);
        when(kafkaProperties.getVideoTopic()).thenReturn(topic);
        when(videoRepository.getByCreatedAtBeforeAndStatus(any(LocalDateTime.class), eq(VideoStatus.IN_PROGRESS.name())))
                .thenReturn(List.of(video1, video2));

        videoCron.checkInProgress();

        verify(kafkaProducer).sendMessage(topic, video1);
        verify(kafkaProducer).sendMessage(topic, video2);
    }

    @Test
    void checkInProgress_WhenKafkaSerializationFails_ShouldThrowRuntimeException() throws Exception {
        int timeoutMinutes = 30;
        String topic = "video-topic";

        Video video = new Video();
        video.setTransactionId("tx-err");

        when(videoProperties.getInProgressTimeout()).thenReturn(timeoutMinutes);
        when(kafkaProperties.getVideoTopic()).thenReturn(topic);
        when(videoRepository.getByCreatedAtBeforeAndStatus(any(LocalDateTime.class), eq(VideoStatus.IN_PROGRESS.name())))
                .thenReturn(List.of(video));

        doThrow(new JsonProcessingException("serialization failed") {})
                .when(kafkaProducer).sendMessage(topic, video);

        RuntimeException ex = assertThrows(RuntimeException.class, () -> videoCron.checkInProgress());

        assertNotNull(ex.getCause());
        assertTrue(ex.getCause() instanceof JsonProcessingException);
    }
}
