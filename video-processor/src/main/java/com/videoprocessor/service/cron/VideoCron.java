package com.videoprocessor.service.cron;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.videoprocessor.constant.VideoStatus;
import com.videoprocessor.model.entity.Video;
import com.videoprocessor.property.KafkaProperties;
import com.videoprocessor.property.VideoProperties;
import com.videoprocessor.repository.VideoRepository;
import com.videoprocessor.service.intf.KafkaProducerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class VideoCron {
    private final VideoRepository videoRepository;
    private final VideoProperties videoProperties;
    private final KafkaProducerService kafkaProducer;
    private final KafkaProperties kafkaProperties;

    @Scheduled(fixedDelay = 1800000)
    public void checkInProgress() {
        LocalDateTime date = LocalDateTime.now().plusMinutes(videoProperties.getInProgressTimeout());

        List<Video> videos = videoRepository.getByCreatedAtBeforeAndStatus(date, VideoStatus.IN_PROGRESS.name());

        videos.forEach(video -> {
            try {
                kafkaProducer.sendMessage(kafkaProperties.getVideoTopic(), video);
                log.info("GifVideo[TransactionId: {}] has been reprocessed.", video.getTransactionId());
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        });
    }
}
