package com.example;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

@MicronautTest(startApplication = false)
class ConfigAndAopTest {
  @Inject Announcer announcer;
  @Inject GreetingConfig config;

  @Test
  void bindsConfigurationPropertiesWithTestEnvironmentOverride() {
    assertEquals("Howdy", config.salutation());
    assertEquals("Howdy ada Howdy ada", announcer.plain("ada"));
  }

  @Test
  void appliesAroundInterceptor() {
    assertEquals("HOWDY ADA HOWDY ADA", announcer.loud("ada"));
  }
}
