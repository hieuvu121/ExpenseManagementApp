package com.be9expensphie.expensphie_backend.config;
//create kafka template

import com.be9expensphie.expensphie_backend.event.EmailEvent;
import com.google.gson.JsonSerializer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaProducerConfig {
    //take var and assign to bootstrap server(ex:9092)
    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Bean
    //create and manage producer with key:string, value:emailEvent
    public ProducerFactory<String, EmailEvent> producerFactory(){
        //contain all config for producer
        Map<String,Object> config=new HashMap<>();
        //set address broker
        config.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG,bootstrapServers);
        config.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        //emailEvent is obj-> jsonSerializer
        config.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        //factory create kafka
        return new DefaultKafkaProducerFactory<String,EmailEvent>(config);
    }

    //send message to kafka
    @Bean
    public KafkaTemplate<String, EmailEvent> kafkaTemplate() {
        return new KafkaTemplate<>(producerFactory());
    }
}
