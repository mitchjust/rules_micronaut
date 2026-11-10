package com.example.langchain4j;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.micronaut.context.annotation.Property;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

@MicronautTest(startApplication = false)
@Property(name = "langchain4j.echo.chat-model.prefix", value = "local ")
class Langchain4jTest {
  @Inject Assistant assistant;

  // Only the generated factory can build EchoChatModel and bind its prefix.
  @Test
  void invokesAiServiceThroughGeneratedModelFactory() {
    assertEquals("local Ada", assistant.chat("Ada"));
  }
}
