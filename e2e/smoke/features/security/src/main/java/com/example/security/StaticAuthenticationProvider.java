package com.example.security;

import io.micronaut.http.HttpRequest;
import io.micronaut.security.authentication.AuthenticationFailureReason;
import io.micronaut.security.authentication.AuthenticationRequest;
import io.micronaut.security.authentication.AuthenticationResponse;
import io.micronaut.security.authentication.provider.HttpRequestAuthenticationProvider;
import jakarta.inject.Singleton;
import java.util.List;

/** Accepts two hard-coded users, one with the reader role, for basic auth in tests. */
@Singleton
public class StaticAuthenticationProvider<B> implements HttpRequestAuthenticationProvider<B> {
  @Override
  public AuthenticationResponse authenticate(
      HttpRequest<B> request, AuthenticationRequest<String, String> auth) {
    if ("ada".equals(auth.getIdentity()) && "lovelace".equals(auth.getSecret())) {
      return AuthenticationResponse.success("ada", List.of("reader"));
    }
    if ("grace".equals(auth.getIdentity()) && "hopper".equals(auth.getSecret())) {
      return AuthenticationResponse.success("grace");
    }
    return AuthenticationResponse.failure(AuthenticationFailureReason.CREDENTIALS_DO_NOT_MATCH);
  }
}
