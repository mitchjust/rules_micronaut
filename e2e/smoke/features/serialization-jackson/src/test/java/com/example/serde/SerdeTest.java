package com.example.serde;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.example.serialization.SharedMessage;
import com.google.devtools.build.runfiles.Runfiles;
import io.micronaut.serde.ObjectMapper;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;

@MicronautTest(startApplication = false)
class SerdeTest {
  @Inject ObjectMapper mapper;

  @Test
  void serializesTypesCompiledThroughEachWiringStyle() throws Exception {
    String json = "{\"message\":\"hello\"}";
    assertEquals(json, mapper.writeValueAsString(new SelectedMessage("hello")));
    assertEquals(json, mapper.writeValueAsString(new PlainMessage("hello")));
    assertEquals(json, mapper.writeValueAsString(new ManualMessage("hello")));
    assertEquals(new SelectedMessage("hello"), mapper.readValue(json, SelectedMessage.class));
    assertEquals(new PlainMessage("hello"), mapper.readValue(json, PlainMessage.class));
    assertEquals(new ManualMessage("hello"), mapper.readValue(json, ManualMessage.class));
  }

  @Test
  void roundTripsModelCompiledWithoutAMapper() throws Exception {
    var message = new SharedMessage("Ada", 42);
    assertEquals(message, mapper.readValue(mapper.writeValueAsBytes(message), SharedMessage.class));
  }

  @Test
  void selectedFeaturesDoNotBringUnrelatedRuntimesOrProcessors() {
    assertThrows(
        ClassNotFoundException.class, () -> Class.forName("com.zaxxer.hikari.HikariDataSource"));
    assertThrows(
        ClassNotFoundException.class,
        () -> Class.forName("io.micronaut.annotation.processing.BeanDefinitionInjectProcessor"));
  }

  @Test
  void inlineApplicationGeneratesSerializationMetadata() throws Exception {
    assertApplication("features.serde_app", "{\"message\":\"inline\"}");
  }

  private static void assertApplication(String property, String expected) throws Exception {
    Runfiles runfiles = Runfiles.preload().unmapped();
    String app = runfiles.rlocation(System.getProperty(property));
    ProcessBuilder builder = new ProcessBuilder(app).redirectErrorStream(true);
    builder.environment().putAll(runfiles.getEnvVars());
    Process process = builder.start();
    try {
      assertTrue(process.waitFor(30, TimeUnit.SECONDS), "Application did not finish");
      String output = new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
      assertEquals(0, process.exitValue(), output);
      assertTrue(output.contains(expected), output);
    } finally {
      process.destroyForcibly();
    }
  }
}
