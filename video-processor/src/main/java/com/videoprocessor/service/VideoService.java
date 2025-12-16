package com.videoprocessor.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.videoprocessor.constant.ErrorCode;
import com.videoprocessor.constant.VideoStatus;
import com.videoprocessor.exception.CommonException;
import com.videoprocessor.model.entity.Video;
import com.videoprocessor.model.request.VideoRequest;
import com.videoprocessor.property.VideoProperties;
import com.videoprocessor.property.KafkaProperties;
import com.videoprocessor.repository.VideoRepository;
import com.videoprocessor.util.FileNameUtils;
import com.videoprocessor.util.StrUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class VideoService {

    private final VideoRepository videoRepository;
    private final VideoProperties videoProperties;
    private final VideoSaveStorageFactory videoSaveStorageFactory;
    private final KafkaProducer kafkaProducer;
    private final KafkaProperties kafkaProperties;

    public Video save(VideoRequest request, MultipartFile file) throws IOException {
        String path = saveFile(file);
        Video video = mapToEntity(request);
        video.setPath(path);
        video.setIsUrl(false);
        Video saveGif = videoRepository.save(video);
        kafkaProducer.sendMessage(kafkaProperties.getVideoTopic(), video);
        return saveGif;
    }

    public Video save(VideoRequest request) throws IOException {
        Video video = mapToEntity(request);
        Video saveGif = videoRepository.save(video);
        video.setIsUrl(true);
        kafkaProducer.sendMessage(kafkaProperties.getVideoTopic(), video);
        return saveGif;
    }

    public Video getVideoTransactionId(String transactionId) {
        return videoRepository.getByTransactionId(transactionId)
                .orElseThrow(() -> new CommonException(ErrorCode.TRANSACTION_NOT_FOUND));
    }

    public byte[] getFile(String fullPath) throws IOException {
        return videoSaveStorageFactory.makeStorageService().getFile(fullPath);
    }

    private Video mapToEntity(VideoRequest request) {
        Video video = new Video();
        video.setStartTime(request.getStartTime());
        video.setEndTime(request.getEndTime());
        video.setTransactionId(StrUtils.UUID());

        if (StringUtils.isNotEmpty(request.getUrl())) {
            video.setPath(request.getUrl());
            video.setIsUrl(true);
        }

        video.setProcessType(request.getProcessType().name());
        video.setPathType(videoProperties.getPathType());
        VideoStatus videoStatus = VideoStatus.IN_PROGRESS;
        if (StringUtils.isNotEmpty(request.getStatus())) {
            videoStatus = VideoStatus.fromValue(request.getStatus());
        }
        video.setStatus(videoStatus.name());
        return video;
    }

    private String saveFile(MultipartFile file) throws IOException {
        String fullPath = fullPath(file);
        byte[] data = file.getBytes();
        if (!videoSaveStorageFactory.makeStorageService().saveFile(fullPath, data)) {
            throw new IOException("Failed to save file");
        }
        return fullPath;
    }

    private String fullPath(MultipartFile file) {
        String path = videoProperties.getFilePath();
        String ext = Objects.requireNonNull(file.getOriginalFilename())
                .substring(file.getOriginalFilename().lastIndexOf(".") + 1);

        return path + "/" + FileNameUtils.GenerateUUIDFileName(ext);
    }

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
