package com.example.websocket;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import io.micronaut.http.client.annotation.Client;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import io.micronaut.websocket.WebSocketClient;
import jakarta.inject.Inject;
import java.net.URI;
import java.time.Duration;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;

@MicronautTest
class WebsocketTest {
  @Inject
  @Client("/")
  WebSocketClient client;

  @Test
  void exchangesMessagesThroughGeneratedClientAndServer() throws Exception {
    try (var connection =
        Mono.from(client.connect(EchoClient.class, URI.create("/echo")))
            .block(Duration.ofSeconds(10))) {
      assertNotNull(connection);
      connection.send("hello");
      assertEquals("echo: hello", connection.awaitMessage());
      connection.send("second message");
      assertEquals("echo: second message", connection.awaitMessage());
    }
  }
}
