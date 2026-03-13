package com.be9expensphie.expensphie_backend.config;

import com.be9expensphie.expensphie_backend.security.WebSocketAuthChannelInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {
    private final WebSocketAuthChannelInterceptor webSocketAuthChannelInterceptor;

    @Override
    //router rq from sender to many receivers
    public void configureMessageBroker(MessageBrokerRegistry config){
        //allow broker know to send data to all client that subscribe to /topic
        config.enableSimpleBroker("/topic");
        //client send /app/...(gateway for client to send rq to server)
        config.setApplicationDestinationPrefixes("/app");
    }

    // Native WebSocket STOMP endpoint for frontend clients
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry){
        registry.addEndpoint("/chat").setAllowedOriginPatterns("*");
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(webSocketAuthChannelInterceptor);
    }

}
