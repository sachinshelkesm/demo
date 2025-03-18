package com.example.demo.consumer;
 
import com.example.demo.model.User;
import com.example.demo.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
 
@Component
public class UserConsumer {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ObjectMapper objectMapper;
 
    @KafkaListener(topics = "user-topic", groupId = "user-group")
    public void listen(String message) throws Exception {
        User user = objectMapper.readValue(message, User.class);
        userRepository.save(user);
    }
}