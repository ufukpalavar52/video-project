package com.videoprocessor.controller;

import com.videoprocessor.model.entity.Video;
import com.videoprocessor.model.request.VideoRequest;
import com.videoprocessor.model.request.VideoUrlRequest;
import com.videoprocessor.service.GifVideoService;
import com.videoprocessor.validator.video.VideoFile;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Valid;
import jakarta.validation.ValidationException;
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
import java.util.Set;
import java.util.stream.Collectors;


@RestController
@RequestMapping("/api/gif")
@RequiredArgsConstructor
public class GifController {

    private final GifVideoService gifVideoService;
    private final Validator validator;


    @GetMapping
    public ResponseEntity<Video> getGifVideo(@RequestParam("transactionId") @NotNull String transactionId) {
        return ResponseEntity.ok(gifVideoService.getVideoTransactionId(transactionId));
    }

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Video> uploadVideo(@RequestPart("file") @VideoFile MultipartFile file,
                                             @RequestParam("startTime") @NotNull Integer startTime,
                                             @RequestParam("endTime") @NotNull Integer endTime) throws IOException {

        VideoRequest videoRequest = VideoRequest.builder().startTime(startTime).endTime(endTime).build();
        validateRequest(videoRequest);
        return ResponseEntity.ok(gifVideoService.save(videoRequest, file));
    }

    @PostMapping(value = "/url")
    public ResponseEntity<Video> uploadUrlVideo(@RequestBody @Valid VideoUrlRequest request) throws IOException {
        return ResponseEntity.ok(gifVideoService.save(request));
    }

    @GetMapping("download/{transactionId}")
    public ResponseEntity<byte[]> download(@PathVariable String transactionId) throws IOException {
        Video video = gifVideoService.getVideoTransactionId(transactionId);
        HttpHeaders headers = new HttpHeaders();

        String fileName = Paths.get(video.getOutputPath()).getFileName().toString();
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"");

        headers.add(HttpHeaders.CONTENT_TYPE, MediaType.IMAGE_GIF_VALUE);

        byte[] fileContent = gifVideoService.getFile(video.getOutputPath());

        headers.setContentLength(fileContent.length);
        return ResponseEntity.ok().headers(headers).body(fileContent);
    }

    private <T> void validateRequest(T request) {
        Set<ConstraintViolation<T>> violations = validator.validate(request);

        if (!violations.isEmpty()) {
            String errorMessages = violations.stream()
                    .map(violation -> violation.getPropertyPath() + ": " + violation.getMessage())
                    .collect(Collectors.joining(", "));
            throw new ValidationException(errorMessages);
        }
    }
}
