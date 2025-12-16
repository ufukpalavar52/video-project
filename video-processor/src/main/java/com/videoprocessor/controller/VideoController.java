package com.videoprocessor.controller;

import com.videoprocessor.constant.VideoProcessType;
import com.videoprocessor.model.entity.Video;
import com.videoprocessor.model.request.VideoRequest;
import com.videoprocessor.service.VideoService;
import com.videoprocessor.util.ValidationUtils;
import com.videoprocessor.validator.video.VideoFile;
import jakarta.validation.Valid;
import jakarta.validation.Validator;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Paths;


@RestController
@RequestMapping("/api/video")
@RequiredArgsConstructor
public class VideoController {

    private final VideoService videoService;
    private final Validator validator;

    @GetMapping
    public ResponseEntity<Video> getTransaction(@RequestParam("transactionId") @NotNull String transactionId) {
        return ResponseEntity.ok(videoService.getVideoTransactionId(transactionId));
    }

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Video> uploadVideo(@RequestPart("file") @VideoFile MultipartFile file,
                                             @RequestParam("startTime") @NotNull Integer startTime,
                                             @RequestParam("endTime") @NotNull Integer endTime,
                                             @RequestParam("processType") @NotNull VideoProcessType processType) throws IOException {

        VideoRequest videoRequest = VideoRequest.builder()
                .startTime(startTime).endTime(endTime).processType(processType).build();
        ValidationUtils.validateRequest(videoRequest, validator);
        return ResponseEntity.ok(videoService.save(videoRequest, file));
    }

    @PostMapping(value = "/url")
    public ResponseEntity<Video> uploadUrlVideo(@RequestBody @Valid VideoRequest request) throws IOException {
        return ResponseEntity.ok(videoService.save(request));
    }

    @GetMapping("download/{transactionId}")
    public ResponseEntity<byte[]> download(@PathVariable String transactionId) throws IOException {
        Video video = videoService.getVideoTransactionId(transactionId);
        HttpHeaders headers = new HttpHeaders();

        String fileName = Paths.get(video.getOutputPath()).getFileName().toString();
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"");


        VideoProcessType processType = VideoProcessType.fromValue(video.getProcessType());
        String contentType = MediaType.IMAGE_GIF_VALUE;
        if (processType != null && !processType.equals(VideoProcessType.GIF)) {
            contentType =  "video/mp4";
        }

        headers.add(HttpHeaders.CONTENT_TYPE, contentType);
        byte[] fileContent = videoService.getFile(video.getOutputPath());

        headers.setContentLength(fileContent.length);
        return ResponseEntity.ok().headers(headers).body(fileContent);
    }
}
