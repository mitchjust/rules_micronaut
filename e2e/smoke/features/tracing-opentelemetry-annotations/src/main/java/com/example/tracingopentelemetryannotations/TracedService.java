package com.example.tracingopentelemetryannotations;

import io.opentelemetry.instrumentation.annotations.SpanAttribute;
import io.opentelemetry.instrumentation.annotations.WithSpan;
import jakarta.inject.Singleton;

@Singleton
public class TracedService {
  @WithSpan("feature.work")
  public String work(@SpanAttribute("person") String name) {
    return "hello " + name;
  }
}
