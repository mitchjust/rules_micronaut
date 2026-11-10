package com.example.opendi;

import static org.junit.jupiter.api.Assertions.assertEquals;

import jakarta.enterprise.inject.se.SeContainerInitializer;
import org.junit.jupiter.api.Test;

class OpenDiTest {
  @Test
  void resolvesCdiProducerAndInjection() {
    // Exercise CDI provider discovery and bootstrap through the CDI SE API.
    try (var container = SeContainerInitializer.newInstance().initialize()) {
      assertEquals("hello CDI", container.select(Consumer.class).get().greet());
    }
  }
}
