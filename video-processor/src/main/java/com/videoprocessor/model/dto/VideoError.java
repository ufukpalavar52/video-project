package com.videoprocessor.model.dto;

import lombok.Data;

@Data
public class VideoError {
    private String message;
    private String transactionId;
}
