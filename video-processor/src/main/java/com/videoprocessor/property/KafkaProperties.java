package com.videoprocessor.property;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Getter
@Configuration
public class KafkaProperties {
    @Value("${spring.kafka.gif-topic}")
    private String gifTopic;

    @Value("${spring.kafka.gif-finish-topic}")
    private String gifFinishTopic;
}
