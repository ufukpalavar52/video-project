package com.videoprocessor.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class KafkaProducerServiceImplTest {

    @Mock
    private KafkaTemplate<String, String> kafkaTemplate;

    @Mock
    private ObjectMapper mapper;

    @InjectMocks
    private KafkaProducerServiceImpl kafkaProducerService;

    @Test
    void sendMessage_WithData_ShouldSerializeAndPublish() throws Exception {
        String topic = "video-topic";
        DummyPayload payload = new DummyPayload("tx-1");
        String json = "{\"transactionId\":\"tx-1\"}";

        when(mapper.writeValueAsString(payload)).thenReturn(json);

        kafkaProducerService.sendMessage(topic, payload);

        verify(mapper).writeValueAsString(payload);
        verify(kafkaTemplate).send(topic, json);
    }

    @Test
    void sendMessage_WithData_WhenSerializationFails_ShouldThrowAndNotPublish() throws Exception {
        String topic = "video-topic";
        DummyPayload payload = new DummyPayload("tx-2");

        when(mapper.writeValueAsString(payload))
                .thenThrow(new JsonProcessingException("serialize failed") {});

        assertThrows(JsonProcessingException.class, () -> kafkaProducerService.sendMessage(topic, payload));

        verify(mapper).writeValueAsString(payload);
        verifyNoInteractions(kafkaTemplate);
    }

    @Test
    void sendMessage_WithRawMessage_ShouldPublishDirectly() {
        String topic = "video-topic";
        String message = "{\"status\":\"ok\"}";

        kafkaProducerService.sendMessage(topic, message);

        verify(kafkaTemplate).send(topic, message);
        verifyNoInteractions(mapper);
    }

    private record DummyPayload(String transactionId) {}
}
