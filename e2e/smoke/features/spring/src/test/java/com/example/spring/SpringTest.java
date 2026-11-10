package com.example.spring;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

@MicronautTest(startApplication = false)
class SpringTest {
  @Inject Consumer consumer;

  @Test
  void mapsSpringComponentsAndInjection() {
    assertEquals("hello", consumer.greet());
  }
}
