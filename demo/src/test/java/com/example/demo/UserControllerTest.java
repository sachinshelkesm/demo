package com.example.demo;

import au.com.dius.pact.consumer.MockServer;
import au.com.dius.pact.consumer.dsl.PactDslWithProvider;
import au.com.dius.pact.consumer.junit5.PactConsumerTestExt;
import au.com.dius.pact.consumer.junit5.PactTestFor;
import au.com.dius.pact.core.model.RequestResponsePact;
import au.com.dius.pact.core.model.annotations.Pact;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(PactConsumerTestExt.class)
public class UserControllerTest {

    @Pact(provider = "UserServiceProvider", consumer = "UserServiceConsumer")
    public RequestResponsePact createPact(PactDslWithProvider builder) {
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");

        return builder
                .given("A user creation request")
                .uponReceiving("A request to create a user")
                .path("/users")
                .method("POST")
                .headers(headers) // Use Map for request headers
                .body("\"John Doe\"")
                .willRespondWith()
                .status(200)
                .headers(headers) // Use Map for response headers
                .body("{\"id\":\"some-id\",\"name\":\"John Doe\"}")
                .toPact();
    }

    @Test
    @PactTestFor(pactMethod = "createPact")
    void testCreateUser(MockServer mockServer) throws Exception {
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpPost post = new HttpPost(mockServer.getUrl() + "/users");
            post.setHeader("Content-Type", "application/json");
            post.setEntity(new StringEntity("\"John Doe\"", ContentType.APPLICATION_JSON));

            var response = client.execute(post);
            try {
                assertEquals(200, response.getCode());
            } finally {
                response.close();
            }
        }
    }
}