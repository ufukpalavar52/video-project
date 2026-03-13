package com.videoprocessor.service.intf;

import com.fasterxml.jackson.core.JsonProcessingException;

public interface KafkaProducerService {
    <T> void sendMessage(String topic, T data) throws JsonProcessingException;
    void sendMessage(String topic, String message);
}
