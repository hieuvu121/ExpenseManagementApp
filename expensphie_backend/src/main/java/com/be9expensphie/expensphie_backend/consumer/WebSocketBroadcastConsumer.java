package com.be9expensphie.expensphie_backend.consumer;

import com.be9expensphie.expensphie_backend.event.WebSocketEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class WebSocketBroadcastConsumer {
    private static final Logger log = LoggerFactory.getLogger(WebSocketBroadcastConsumer.class);

    private final SimpMessagingTemplate messagingTemplate;
    private final ObjectMapper objectMapper;

    @KafkaListener(
            topics = "websocket-events",
            groupId = "${kafka.websocket.group-id}",
            containerFactory = "wsKafkaListenerContainerFactory"
    )
    public void consume(WebSocketEvent event) {
        try {
            Object payload = objectMapper.readValue(event.getPayload(), Object.class);
            messagingTemplate.convertAndSend(event.getDestination(), payload);
        } catch (JsonProcessingException e) {
            log.error("Failed to deserialize WebSocket payload for destination {}", event.getDestination(), e);
        }
    }
}
