package com.example.servlet;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.micronaut.context.annotation.Property;
import io.micronaut.runtime.server.EmbeddedServer;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import org.junit.jupiter.api.Test;

@MicronautTest
@Property(name = "micronaut.server.port", value = "-1")
class ServletTest {
  @Inject EmbeddedServer server;

  @Test
  void registersAnnotatedServletAndInjectsItsDependencies() throws Exception {
    try (var client = HttpClient.newHttpClient()) {
      var response =
          client.send(
              HttpRequest.newBuilder(server.getURI().resolve("/hello?name=Ada")).build(),
              HttpResponse.BodyHandlers.ofString());
      assertEquals(200, response.statusCode());
      assertEquals("hello Ada", response.body());
    }
  }
}
