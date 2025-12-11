package com.videoprocessor.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.videoprocessor.constant.GifVideoStatus;
import com.videoprocessor.model.dto.GifVideoError;
import com.videoprocessor.model.entity.GifVideo;
import com.videoprocessor.model.entity.VideoErrorLog;
import com.videoprocessor.repository.VideoErrorLogRepository;
import com.videoprocessor.repository.GifVideoRepository;
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
    private final GifVideoRepository gifVideoRepository;
    private final VideoErrorLogRepository errorLogRepository;
    private final ObjectMapper mapper;
    private final VideoSaveStorageFactory videoSaveStorageFactory;

    @KafkaListener(topics = "${spring.kafka.gif-finish-topic}", groupId = "${spring.kafka.consumer.group-id}")
    public void consumeGifFinish(String message) throws JsonProcessingException {
        GifVideo gifVideo = mapper.readValue(message, GifVideo.class);
        gifVideo.setStatus(GifVideoStatus.SUCCESS.name());
        gifVideoRepository.save(gifVideo);
        log.info("Gif process finished {}", gifVideo);
        Boolean isUrl = Optional.ofNullable(gifVideo.getIsUrl()).orElse(false);
        if (isUrl) {
            return;
        }
        deleteVideo(gifVideo.getPath(), gifVideo.getPathType());
    }

    @KafkaListener(topics = "${spring.kafka.gif-error-topic}", groupId = "${spring.kafka.consumer.group-id}")
    public void consumeGifError(String message) throws JsonProcessingException {
        GifVideoError gifError = mapper.readValue(message, GifVideoError.class);
        GifVideo gifVideo = gifVideoRepository.getByTransactionId(gifError.getTransactionId()).orElse(null);
        if (gifVideo == null) {
            log.warn("Gif process not found {}", gifError);
            return;
        }
        gifVideo.setStatus(GifVideoStatus.ERROR.name());
        gifVideoRepository.save(gifVideo);

        String truncatedMessage = StrUtils.truncateString(gifError.getMessage(), 5000);

        VideoErrorLog errorLog = new VideoErrorLog();
        errorLog.setTransactionId(gifError.getTransactionId());
        errorLog.setMessage(truncatedMessage);
        errorLogRepository.save(errorLog);

        Boolean isUrl = Optional.ofNullable(gifVideo.getIsUrl()).orElse(false);
        if (isUrl) {
            return;
        }
        deleteVideo(gifVideo.getPath(), gifVideo.getPathType());
    }


    public void deleteVideo(String path, String pathType)  {
        try {
            videoSaveStorageFactory.makeStorageService(pathType).deleteFile(path);
        } catch (Exception exception) {
            log.error("Failed to delete video {}", path, exception);
        }
    }
}
