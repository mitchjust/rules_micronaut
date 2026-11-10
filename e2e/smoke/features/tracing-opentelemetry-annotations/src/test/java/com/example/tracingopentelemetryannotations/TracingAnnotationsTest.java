package com.example.tracingopentelemetryannotations;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.micronaut.context.annotation.Bean;
import io.micronaut.context.annotation.Factory;
import io.micronaut.context.annotation.Property;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.sdk.testing.exporter.InMemorySpanExporter;
import io.opentelemetry.sdk.trace.SpanProcessor;
import io.opentelemetry.sdk.trace.export.SimpleSpanProcessor;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.junit.jupiter.api.Test;

@MicronautTest(startApplication = false)
@Property(name = "otel.traces.exporter", value = "none")
@Property(name = "otel.metrics.exporter", value = "none")
@Property(name = "otel.logs.exporter", value = "none")
class TracingAnnotationsTest {
  @Inject TracedService service;
  @Inject InMemorySpanExporter exporter;

  @Test
  void exportsAnnotatedSpanInMemory() {
    assertEquals("hello Ada", service.work("Ada"));
    var spans = exporter.getFinishedSpanItems();
    assertEquals(1, spans.size());
    assertEquals("TracedService.work#feature.work", spans.getFirst().getName());
    assertEquals("Ada", spans.getFirst().getAttributes().get(AttributeKey.stringKey("person")));
  }

  @Factory
  static class TelemetryFactory {
    @Singleton
    InMemorySpanExporter exporter() {
      return InMemorySpanExporter.create();
    }

    // The processor owns the exporter and closes it with the test context.
    @Singleton
    @Bean(preDestroy = "close")
    SpanProcessor processor(InMemorySpanExporter exporter) {
      return SimpleSpanProcessor.create(exporter);
    }
  }
}
