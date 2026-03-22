package com.be9expensphie.expensphie_backend.producer;
//send event to kafka

import com.be9expensphie.expensphie_backend.event.EmailEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailProducer {
    private final KafkaTemplate<String, EmailEvent> kafkaTemplate;

    @Value("${kafka.topic.email}")
    private String emailTopic;

    //receive input from service->create event-> send to kafka
    public void sendEmailEvent(String to,String subject, String body, String eventType){
        EmailEvent event = new EmailEvent(to, subject, body, eventType, null);
        //log to debug
        log.info("Sending email event to Kafka topic: {} for {}",emailTopic, to);
        kafkaTemplate.send(emailTopic,to,event)
                //funct to address async
                .whenComplete((result,ex)->{
                    if(ex==null){
                        log.info("Email event sent succsessfully: {}",result.getRecordMetadata());
                    }else{
                        log.error("Failed to send email event",ex);
                    }
                });
    }
}
