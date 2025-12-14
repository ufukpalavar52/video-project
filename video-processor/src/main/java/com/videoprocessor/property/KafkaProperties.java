package com.videoprocessor.property;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Getter
@Configuration
public class KafkaProperties {
    @Value("${spring.kafka.video-topic}")
    private String videoTopic;

    @Value("${spring.kafka.video-finish-topic}")
    private String videoFinishTopic;
}
