package com.example.httpclientjdk;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.client.HttpClient;
import io.micronaut.http.client.annotation.Client;
import io.micronaut.http.client.exceptions.HttpClientResponseException;
import io.micronaut.http.client.jdk.JdkHttpClient;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

@MicronautTest
class JdkHttpClientTest {
  @Inject GreetingClient greetings;

  @Inject
  @Client("/")
  HttpClient client;

  @Test
  void selectsJdkTransportAndPerformsHttpRequests() {
    assertInstanceOf(JdkHttpClient.class, client);
    assertEquals("Hello, Ada", client.toBlocking().retrieve(HttpRequest.GET("/greet/Ada")));
  }

  @Test
  void generatesAndInjectsDeclarativeClient() {
    assertEquals("Hello, Grace", greetings.greet("Grace"));
  }

  @Test
  void propagatesHttpErrors() {
    var error =
        assertThrows(
            HttpClientResponseException.class,
            () -> client.toBlocking().retrieve(HttpRequest.GET("/missing")));
    assertEquals(HttpStatus.NOT_FOUND, error.getStatus());
  }
}
