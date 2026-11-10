package com.example.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.client.HttpClient;
import io.micronaut.http.client.annotation.Client;
import io.micronaut.http.client.exceptions.HttpClientResponseException;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

@MicronautTest
class AccountControllerTest {
  @Inject
  @Client("/")
  HttpClient client;

  @Test
  void rejectsAnonymousRequests() {
    HttpClientResponseException e =
        assertThrows(
            HttpClientResponseException.class,
            () -> client.toBlocking().retrieve(HttpRequest.GET("/account")));
    assertEquals(HttpStatus.UNAUTHORIZED, e.getStatus());
  }

  @Test
  void rejectsWrongPassword() {
    HttpClientResponseException e =
        assertThrows(
            HttpClientResponseException.class,
            () -> client.toBlocking().retrieve(HttpRequest.GET("/account").basicAuth("ada", "nope")));
    assertEquals(HttpStatus.UNAUTHORIZED, e.getStatus());
  }

  @Test
  void returnsPrincipalForValidBasicAuth() {
    assertEquals(
        "ada", client.toBlocking().retrieve(HttpRequest.GET("/account").basicAuth("ada", "lovelace")));
  }

  @Test
  void allowsAnonymousOnIsAnonymousRoutes() {
    assertEquals("anyone", client.toBlocking().retrieve(HttpRequest.GET("/account/public")));
  }

  @Test
  void allowsAnonymousWithJakartaPermitAll() {
    assertEquals("anyone", client.toBlocking().retrieve(HttpRequest.GET("/account/jakarta-public")));
  }

  @Test
  void allowsMatchingJakartaRole() {
    assertEquals(
        "readable",
        client
            .toBlocking()
            .retrieve(HttpRequest.GET("/account/jakarta-reader").basicAuth("ada", "lovelace")));
  }

  @Test
  void rejectsMissingJakartaRole() {
    HttpClientResponseException e =
        assertThrows(
            HttpClientResponseException.class,
            () ->
                client
                    .toBlocking()
                    .retrieve(HttpRequest.GET("/account/jakarta-reader").basicAuth("grace", "hopper")));
    assertEquals(HttpStatus.FORBIDDEN, e.getStatus());
  }

  @Test
  void rejectsAuthenticatedUsersWithJakartaDenyAll() {
    HttpClientResponseException e =
        assertThrows(
            HttpClientResponseException.class,
            () ->
                client
                    .toBlocking()
                    .retrieve(HttpRequest.GET("/account/jakarta-denied").basicAuth("ada", "lovelace")));
    assertEquals(HttpStatus.FORBIDDEN, e.getStatus());
  }
}
