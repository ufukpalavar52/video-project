package com.videoprocessor.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.videoprocessor.constant.ErrorCode;
import com.videoprocessor.constant.GifVideoStatus;
import com.videoprocessor.exception.CommonException;
import com.videoprocessor.model.entity.GifVideo;
import com.videoprocessor.model.request.GifRequest;
import com.videoprocessor.model.request.GifUrlRequest;
import com.videoprocessor.property.VideoProperties;
import com.videoprocessor.property.KafkaProperties;
import com.videoprocessor.repository.GifVideoRepository;
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
public class GifVideoService {

    private final GifVideoRepository gifVideoRepository;
    private final VideoProperties videoProperties;
    private final VideoSaveStorageFactory videoSaveStorageFactory;
    private final KafkaProducer kafkaProducer;
    private final KafkaProperties kafkaProperties;

    public GifVideo save(GifRequest request, MultipartFile file) throws IOException {
        String path = saveFile(file);
        GifVideo gifVideo = mapToEntity(request);
        gifVideo.setPath(path);
        gifVideo.setTransactionId(StrUtils.UUID());
        GifVideo saveGif = gifVideoRepository.save(gifVideo);
        kafkaProducer.sendMessage(kafkaProperties.getGifTopic(), gifVideo);
        return saveGif;
    }

    public GifVideo save(GifUrlRequest request) throws IOException {
        GifVideo gifVideo = mapToEntity(request);
        gifVideo.setTransactionId(StrUtils.UUID());
        GifVideo saveGif = gifVideoRepository.save(gifVideo);
        kafkaProducer.sendMessage(kafkaProperties.getGifTopic(), gifVideo);
        return saveGif;
    }

    public GifVideo getVideoTransactionId(String transactionId) {
        return gifVideoRepository.getByTransactionId(transactionId)
                .orElseThrow(() -> new CommonException(ErrorCode.TRANSACTION_NOT_FOUND));
    }

    public byte[] getFile(String fullPath) throws IOException {
        return videoSaveStorageFactory.makeStorageService().getFile(fullPath);
    }

    private GifVideo mapToEntity(GifRequest request) {
        GifVideo gifVideo = new GifVideo();
        gifVideo.setStartTime(request.getStartTime());
        gifVideo.setEndTime(request.getEndTime());
        gifVideo.setIsUrl(false);
        gifVideo.setPathType(videoProperties.getPathType());
        GifVideoStatus gifVideoStatus = GifVideoStatus.IN_PROGRESS;
        if (StringUtils.isNotEmpty(request.getStatus())) {
            gifVideoStatus = GifVideoStatus.fromValue(request.getStatus());
        }
        gifVideo.setStatus(gifVideoStatus.name());
        return gifVideo;
    }

    private GifVideo mapToEntity(GifUrlRequest request) {
        GifVideo gifVideo = new GifVideo();
        gifVideo.setStartTime(request.getStartTime());
        gifVideo.setEndTime(request.getEndTime());
        gifVideo.setIsUrl(true);
        gifVideo.setPath(request.getUrl());
        gifVideo.setPathType(videoProperties.getPathType());
        GifVideoStatus gifVideoStatus = GifVideoStatus.IN_PROGRESS;
        if (StringUtils.isNotEmpty(request.getStatus())) {
            gifVideoStatus = GifVideoStatus.fromValue(request.getStatus());
        }
        gifVideo.setStatus(gifVideoStatus.name());
        return gifVideo;
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

        List<GifVideo> gifVideos = gifVideoRepository.getByCreatedAtBeforeAndStatus(date, GifVideoStatus.IN_PROGRESS.name());

        gifVideos.forEach(gifVideo -> {
            try {
                kafkaProducer.sendMessage(kafkaProperties.getGifTopic(), gifVideo);
                log.info("GifVideo[TransactionId: {}] has been reprocessed.", gifVideo.getTransactionId());
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        });
    }
}
