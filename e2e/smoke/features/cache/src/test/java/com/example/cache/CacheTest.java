package com.example.cache;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.micronaut.context.annotation.Property;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

@MicronautTest(startApplication = false)
@Property(name = "micronaut.caches.greetings.maximum-size", value = "10")
class CacheTest {
  @Inject GreetingService service;

  @Test
  void cachesByKeyAndInvalidatesOnlyTheSelectedEntry() {
    assertEquals("Hello, Ada", service.greet("Ada"));
    assertEquals("Hello, Ada", service.greet("Ada"));
    assertEquals(1, service.invocations());

    assertEquals("Hello, Grace", service.greet("Grace"));
    assertEquals(2, service.invocations());

    service.invalidate("Ada");

    assertEquals("Hello, Grace", service.greet("Grace"));
    assertEquals(2, service.invocations());
    assertEquals("Hello, Ada", service.greet("Ada"));
    assertEquals(3, service.invocations());
    assertEquals("Hello, Ada", service.greet("Ada"));
    assertEquals(3, service.invocations());
  }
}
