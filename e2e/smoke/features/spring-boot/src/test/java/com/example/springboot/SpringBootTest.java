package com.example.springboot;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.micronaut.context.annotation.Property;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

@MicronautTest(startApplication = false)
@Property(name = "greeting.message", value = "hello boot")
class SpringBootTest {
  @Inject GreetingProperties properties;

  @Test
  void mapsBootConfigurationProperties() {
    assertEquals("hello boot", properties.getMessage());
  }
}
