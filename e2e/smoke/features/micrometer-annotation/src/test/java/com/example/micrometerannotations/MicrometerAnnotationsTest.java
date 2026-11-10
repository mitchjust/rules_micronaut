package com.example.micrometerannotations;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.micrometer.core.instrument.MeterRegistry;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

@MicronautTest(startApplication = false)
class MicrometerAnnotationsTest {
  @Inject MeasuredService service;
  @Inject MeterRegistry registry;

  @Test
  void instrumentsBeanMethods() {
    assertEquals("done", service.work());
    assertEquals(1.0, registry.get("feature.calls").counter().count());
    assertEquals(1, registry.get("feature.duration").timer().count());
  }
}
