package com.videoprocessor.property;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
public class S3Properties {
    @Value("${s3.endpoint}")
    private String endpoint;

    @Value("${s3.accessKey}")
    private String accessKey;

    @Value("${s3.secretKey}")
    private String secretKey;

    @Value("${s3.region}")
    private String region;

    @Value("${s3.type:custom}")
    private String s3Type;

    public Boolean isPathEnabled() {
        return s3Type.equalsIgnoreCase("custom");
    }
}
