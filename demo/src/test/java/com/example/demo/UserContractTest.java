package com.example.demo;
 
import au.com.dius.pact.consumer.MessagePactBuilder;
import au.com.dius.pact.consumer.dsl.PactDslJsonBody;
import au.com.dius.pact.consumer.junit5.PactConsumerTestExt;
import au.com.dius.pact.consumer.junit5.PactTestFor;
import au.com.dius.pact.consumer.junit5.ProviderType;
import au.com.dius.pact.core.model.annotations.Pact;
import com.example.demo.consumer.UserConsumer;
import com.example.demo.model.User;
import com.example.demo.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
 
import java.util.List;
 
import static org.junit.jupiter.api.Assertions.assertEquals;
 
@SpringBootTest
@Testcontainers
@ExtendWith(PactConsumerTestExt.class)
public class UserContractTest {
 
    @Container
    private static final KafkaContainer kafkaContainer = new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:latest"));
 
    @Container
    private static final MongoDBContainer mongoDBContainer = new MongoDBContainer(DockerImageName.parse("mongo:latest"));
 
    @Autowired
    private UserConsumer userConsumer;
 
    @Autowired
    private UserRepository userRepository;
 
    @Autowired
    private ObjectMapper objectMapper;
 
    @DynamicPropertySource
    static void overrideProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.kafka.bootstrap-servers", kafkaContainer::getBootstrapServers);
        registry.add("spring.data.mongodb.uri", mongoDBContainer::getReplicaSetUrl);
    }
 
    @Pact(consumer = "UserConsumer", provider = "UserProducer")
    public MessagePactBuilder createPact(MessagePactBuilder builder) {
        PactDslJsonBody body = new PactDslJsonBody()
                .stringType("name", "John Doe")
                .stringType("email", "john@example.com");
 
        return builder
                .expectsToReceive("a user registration event")
                .withContent(body)
                .toPact();
    }
 
    @Test
    @PactTestFor(pactMethod = "createPact", providerType = ProviderType.ASYNCH)
    public void testUserConsumer(List<au.com.dius.pact.core.model.messaging.Message> messages) throws Exception {
        // Simulate receiving the message
        String messageContent = new String(messages.get(0).contentsAsBytes());
        userConsumer.listen(messageContent);
 
        // Verify the user was saved to MongoDB
        List<User> users = userRepository.findAll();
        assertEquals(1, users.size());
        assertEquals("John Doe", users.get(0).getName());
        assertEquals("john@example.com", users.get(0).getEmail());
    }
}