package com.videoprocessor.property;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
public class VideoProperties {
    @Value("${video.file.pathType:file}")
    private String pathType;

    @Value("${video.file.path}")
    private String filePath;

    @Value("${video.gif.in-progress.timeout:30}")
    private Integer inProgressTimeout;

    @Value("${video.gif.timeout-days:7}")
    private Integer gifVideoTimeoutDays;
}
