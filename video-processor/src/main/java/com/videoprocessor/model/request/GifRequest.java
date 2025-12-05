package com.videoprocessor.model.request;

import com.videoprocessor.validator.greater.IsGreaterThan;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@IsGreaterThan(
        field = "startTime",
        greaterThanField = "endTime",
        message = "The start time cannot occur after the end time."
)
public class GifRequest {
    private String path;
    private int startTime;
    private int endTime;
    private String status;
}
