package com.be9expensphie.expensphie_backend.consumer;

import com.be9expensphie.expensphie_backend.event.WebSocketEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class WebSocketBroadcastConsumer {
    private final SimpMessagingTemplate messagingTemplate;

    @KafkaListener(
            topics = "websocket-events",
            groupId = "${kafka.websocket.group-id}",
            containerFactory = "wsKafkaListenerContainerFactory"
    )
    public void consume(WebSocketEvent event){
        messagingTemplate.convertAndSend(event.getDestination(),event.getPayload());
    }
}
