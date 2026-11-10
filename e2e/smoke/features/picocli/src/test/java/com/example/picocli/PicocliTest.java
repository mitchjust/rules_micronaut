package com.example.picocli;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.micronaut.configuration.picocli.PicocliRunner;
import io.micronaut.context.ApplicationContext;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;

@MicronautTest(startApplication = false)
class PicocliTest {
  @Inject ApplicationContext context;

  @Test
  void injectsAndExecutesCommand() {
    assertEquals(3, PicocliRunner.call(GreetingCommand.class, context, "--name", "Ada"));
  }

  @Test
  void packagesProcessorGeneratedNativeConfiguration() throws Exception {
    try (var input =
        GreetingCommand.class.getResourceAsStream(
            "/META-INF/native-image/picocli-generated/reflect-config.json")) {
      assertNotNull(input, "Picocli's standalone processor must run");
      assertTrue(
          new String(input.readAllBytes(), StandardCharsets.UTF_8).contains("GreetingCommand"));
    }
  }
}
