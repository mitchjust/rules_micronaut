package com.example.retry;

import io.micronaut.retry.annotation.Recoverable;
import jakarta.inject.Singleton;

@Singleton
@Recoverable(api = Greeting.class)
public class RemoteGreeting implements Greeting {
  @Override
  public String greet(String name) {
    throw new IllegalStateException("Remote service unavailable");
  }
}
