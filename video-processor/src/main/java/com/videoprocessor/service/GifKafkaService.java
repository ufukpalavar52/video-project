package com.videoprocessor.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.videoprocessor.constant.VideoStatus;
import com.videoprocessor.model.dto.VideoError;
import com.videoprocessor.model.entity.Video;
import com.videoprocessor.model.entity.VideoErrorLog;
import com.videoprocessor.repository.VideoErrorLogRepository;
import com.videoprocessor.repository.VideoRepository;
import com.videoprocessor.util.StrUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class GifKafkaService {
    private final VideoRepository videoRepository;
    private final VideoErrorLogRepository errorLogRepository;
    private final ObjectMapper mapper;
    private final VideoSaveStorageFactory videoSaveStorageFactory;

    @KafkaListener(topics = "${spring.kafka.video-finish-topic}", groupId = "${spring.kafka.consumer.group-id}")
    public void consumeGifFinish(String message) throws JsonProcessingException {
        Video video = mapper.readValue(message, Video.class);
        video.setStatus(VideoStatus.SUCCESS.name());
        videoRepository.save(video);
        log.info("Gif process finished {}", video);
        Boolean isUrl = Optional.ofNullable(video.getIsUrl()).orElse(false);
        if (isUrl) {
            return;
        }
        deleteVideo(video.getPath(), video.getPathType());
    }

    @KafkaListener(topics = "${spring.kafka.video-error-topic}", groupId = "${spring.kafka.consumer.group-id}")
    public void consumeGifError(String message) throws JsonProcessingException {
        VideoError gifError = mapper.readValue(message, VideoError.class);
        Video video = videoRepository.getByTransactionId(gifError.getTransactionId()).orElse(null);
        if (video == null) {
            log.warn("Gif process not found {}", gifError);
            return;
        }
        video.setStatus(VideoStatus.ERROR.name());
        videoRepository.save(video);

        String truncatedMessage = StrUtils.truncateString(gifError.getMessage(), 5000);

        VideoErrorLog errorLog = new VideoErrorLog();
        errorLog.setTransactionId(gifError.getTransactionId());
        errorLog.setMessage(truncatedMessage);
        errorLogRepository.save(errorLog);

        Boolean isUrl = Optional.ofNullable(video.getIsUrl()).orElse(false);
        if (isUrl) {
            return;
        }
        deleteVideo(video.getPath(), video.getPathType());
    }


    public void deleteVideo(String path, String pathType)  {
        try {
            videoSaveStorageFactory.makeStorageService(pathType).deleteFile(path);
        } catch (Exception exception) {
            log.error("Failed to delete video {}", path, exception);
        }
    }
}
