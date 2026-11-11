package com.example.retry;

import io.micronaut.retry.annotation.Fallback;
import jakarta.inject.Singleton;

@Singleton
@Fallback
public class LocalGreeting implements Greeting {
  @Override
  public String greet(String name) {
    return "Hello, " + name;
  }
}
