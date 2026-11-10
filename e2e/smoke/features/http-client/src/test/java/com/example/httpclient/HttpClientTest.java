package com.example.httpclient;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

@MicronautTest
class HttpClientTest {
  @Inject GreetingClient greetingClient;

  @Test
  void generatesAndInjectsDeclarativeClient() {
    assertEquals("{\"message\":\"Hello, client!\"}", greetingClient.greet("client"));
  }
}
