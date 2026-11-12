package com.example.management;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.client.HttpClient;
import io.micronaut.http.client.annotation.Client;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import java.util.Map;
import org.junit.jupiter.api.Test;

@MicronautTest
class ManagementTest {
  @Inject
  @Client("/")
  HttpClient client;

  @Test
  void exposesBuiltInHealthEndpoint() {
    var response = client.toBlocking().exchange(HttpRequest.GET("/health"), Map.class);
    assertEquals(HttpStatus.OK, response.status());
    assertEquals("UP", response.body().get("status"));
  }

  @Test
  void routesCustomEndpointFromSeparateLibrary() {
    var response = client.toBlocking().retrieve(HttpRequest.GET("/inventory"), Map.class);
    assertEquals(3, response.get("books"));
  }
}
