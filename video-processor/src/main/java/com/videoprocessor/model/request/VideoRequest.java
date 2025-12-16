package com.videoprocessor.model.request;

import com.videoprocessor.constant.VideoProcessType;
import com.videoprocessor.validator.greater.IsGreaterThan;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.URL;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@IsGreaterThan(
        field = "startTime",
        greaterThanField = "endTime",
        message = "The start time cannot occur after the end time."
)
public class VideoRequest {
    @URL
    private String url;
    private String path;
    private int startTime;
    private int endTime;
    private String status;
    private VideoProcessType processType;
}
