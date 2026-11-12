package com.example.securityjwt;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import io.micronaut.context.annotation.Property;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.client.HttpClient;
import io.micronaut.http.client.annotation.Client;
import io.micronaut.http.client.exceptions.HttpClientResponseException;
import io.micronaut.security.authentication.Authentication;
import io.micronaut.security.token.jwt.generator.JwtTokenGenerator;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import java.util.List;
import org.junit.jupiter.api.Test;

@MicronautTest
@Property(name = "micronaut.security.authentication", value = "bearer")
@Property(
    name = "micronaut.security.token.jwt.signatures.secret.generator.secret",
    value = "test-only-signing-secret-at-least-256-bits-long")
class SecurityJwtTest {
  @Inject
  @Client("/")
  HttpClient client;

  @Inject JwtTokenGenerator tokens;

  @Test
  void acceptsSignedTokenAndPropagatesPrincipal() {
    var token =
        tokens.generateToken(Authentication.build("ada", List.of("reader")), 60).orElseThrow();
    assertEquals(
        "ada", client.toBlocking().retrieve(HttpRequest.GET("/jwt-account").bearerAuth(token)));
  }

  @Test
  void rejectsMissingToken() {
    var failure =
        assertThrows(
            HttpClientResponseException.class,
            () -> client.toBlocking().retrieve(HttpRequest.GET("/jwt-account")));
    assertEquals(HttpStatus.UNAUTHORIZED, failure.getStatus());
  }

  @Test
  void rejectsTamperedSignature() {
    var token =
        tokens.generateToken(Authentication.build("ada", List.of("reader")), 60).orElseThrow();
    int signatureStart = token.lastIndexOf('.') + 1;
    var tampered =
        token.substring(0, signatureStart)
            + (token.charAt(signatureStart) == 'A' ? "B" : "A")
            + token.substring(signatureStart + 1);
    assertRejected(tampered, HttpStatus.UNAUTHORIZED);
  }

  @Test
  void rejectsExpiredToken() {
    var expired =
        tokens.generateToken(Authentication.build("ada", List.of("reader")), -60).orElseThrow();
    assertRejected(expired, HttpStatus.UNAUTHORIZED);
  }

  @Test
  void rejectsTokenWithoutRequiredRole() {
    var token = tokens.generateToken(Authentication.build("grace"), 60).orElseThrow();
    assertRejected(token, HttpStatus.FORBIDDEN);
  }

  private void assertRejected(String token, HttpStatus status) {
    var failure =
        assertThrows(
            HttpClientResponseException.class,
            () -> client.toBlocking().retrieve(HttpRequest.GET("/jwt-account").bearerAuth(token)));
    assertEquals(status, failure.getStatus());
  }
}
