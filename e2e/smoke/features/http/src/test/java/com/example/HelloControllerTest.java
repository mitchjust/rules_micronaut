package com.example;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.micronaut.http.client.HttpClient;
import io.micronaut.http.client.annotation.Client;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

@MicronautTest
class HelloControllerTest {
  @Inject
  @Client("/")
  HttpClient client;

  @Test
  void greetsByName() {
    Greeting greeting = client.toBlocking().retrieve("/hello/bazel", Greeting.class);
    assertEquals("Hello, bazel!", greeting.message());
  }
}
