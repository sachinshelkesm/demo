package com.example.demo.producer;
 
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
 
@Component
public class UserProducer {
    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;
 
    public void sendUser(String userJson) {
        kafkaTemplate.send("user-topic", userJson);
    }
}