package com.example;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import io.micronaut.http.HttpStatus;
import io.micronaut.http.client.HttpClient;
import io.micronaut.http.client.annotation.Client;
import io.micronaut.http.client.exceptions.HttpClientResponseException;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

@MicronautTest
class ValidationTest {
  @Inject
  @Client("/")
  HttpClient client;

  @Test
  void acceptsValidInput() {
    assertEquals("Hi, ada", client.toBlocking().retrieve("/strict/ada"));
  }

  @Test
  void rejectsInvalidInputWith400() {
    HttpClientResponseException e =
        assertThrows(
            HttpClientResponseException.class,
            () -> client.toBlocking().retrieve("/strict/much-too-long"));
    assertEquals(HttpStatus.BAD_REQUEST, e.getStatus());
  }
}
