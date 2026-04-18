package com.be9expensphie.expensphie_backend.config;

import com.be9expensphie.expensphie_backend.event.WebSocketEvent;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.support.serializer.JsonDeserializer;


import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaConsumerConfig {
    @Value("${spring.kafka.bootstrap-servers}")
    private String bootStrapServers;

    @Bean
    ConsumerFactory<String, WebSocketEvent> wsConsumerFactory(){
        Map<String, Object> config = new HashMap<>();
        config.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootStrapServers);
        config.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        config.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        config.put(JsonDeserializer.TRUSTED_PACKAGES, "com.be9expensphie.expensphie_backend.event");
        config.put(JsonDeserializer.VALUE_DEFAULT_TYPE, WebSocketEvent.class.getName());
        return new DefaultKafkaConsumerFactory<>(config);
    }

    //act like a container pool mess constantly
    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, WebSocketEvent> wsKafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, WebSocketEvent> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(wsConsumerFactory());
        return factory;
    }
}
