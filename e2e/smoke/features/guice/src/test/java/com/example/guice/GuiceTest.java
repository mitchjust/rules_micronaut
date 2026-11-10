package com.example.guice;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

@MicronautTest(startApplication = false)
class GuiceTest {
  @Inject Greeting greeting;

  @Test
  void importsGuiceModuleBindings() {
    assertEquals("hello guice", greeting.hello());
  }
}
