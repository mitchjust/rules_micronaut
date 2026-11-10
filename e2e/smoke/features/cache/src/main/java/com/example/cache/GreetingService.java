package com.example.cache;

import io.micronaut.cache.annotation.CacheInvalidate;
import io.micronaut.cache.annotation.Cacheable;
import jakarta.inject.Singleton;
import java.util.concurrent.atomic.AtomicInteger;

@Singleton
public class GreetingService {
  private final AtomicInteger invocations = new AtomicInteger();

  @Cacheable("greetings")
  public String greet(String name) {
    invocations.incrementAndGet();
    return "Hello, " + name;
  }

  @CacheInvalidate("greetings")
  public void invalidate(String name) {}

  public int invocations() {
    return invocations.get();
  }
}
