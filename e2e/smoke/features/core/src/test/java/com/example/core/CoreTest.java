package com.example.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.devtools.build.runfiles.Runfiles;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;

class CoreTest {
  @Test
  void testRuntimeDoesNotIncludeOptionalSupport() {
    assertThrows(ClassNotFoundException.class, () -> Class.forName("org.yaml.snakeyaml.Yaml"));
    assertThrows(
        ClassNotFoundException.class, () -> Class.forName("ch.qos.logback.classic.LoggerContext"));
  }

  @Test
  void applicationRunsWithoutOtherFeaturesOnItsClasspath() throws Exception {
    Runfiles runfiles = Runfiles.preload().unmapped();
    String app = runfiles.rlocation(System.getProperty("features.core_app"));
    ProcessBuilder builder = new ProcessBuilder(app).redirectErrorStream(true);
    builder.environment().putAll(runfiles.getEnvVars());
    Process process = builder.start();
    try {
      assertTrue(process.waitFor(30, TimeUnit.SECONDS), "Application did not finish");
      String output = new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
      assertEquals(0, process.exitValue(), output);
      assertTrue(output.contains("core only"), output);
    } finally {
      process.destroyForcibly();
    }
  }
}
