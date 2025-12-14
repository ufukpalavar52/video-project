package com.videoprocessor.model.request;

import com.videoprocessor.validator.greater.IsGreaterThan;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.hibernate.validator.constraints.URL;

@Data
@IsGreaterThan(
        field = "startTime",
        greaterThanField = "endTime",
        message = "The start time cannot occur after the end time."
)
public class VideoUrlRequest {
    @URL
    private String url;
    @NotNull
    private int startTime;
    @NotNull
    private int endTime;
    private String status;
}
