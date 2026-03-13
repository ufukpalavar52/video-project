package com.videoprocessor.service.intf;

import com.videoprocessor.model.entity.Video;
import com.videoprocessor.model.request.VideoRequest;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface VideoService {
    Video save(VideoRequest request, MultipartFile file) throws IOException;
    Video save(VideoRequest request) throws IOException;
    Video getVideoTransactionId(String transactionId);
    byte[] getFile(String fullPath) throws IOException;
}
