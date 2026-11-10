package com.example.macros;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import com.google.devtools.build.runfiles.Runfiles;
import io.micronaut.context.BeanContext;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("macro")
class MacroTest {
  @Test
  void exportedBeansAndResourcesReachTheTest() throws Exception {
    try (BeanContext context = BeanContext.run()) {
      assertEquals("hello", context.getBean(Greeting.class).getMessage());
    }
    try (var resource = getClass().getResourceAsStream("/library.txt")) {
      assertEquals("library", new String(resource.readAllBytes(), StandardCharsets.UTF_8).trim());
    }
  }

  @Test
  void inlineApplicationProcessesBeansAndResources() throws Exception {
    Runfiles runfiles = Runfiles.preload().unmapped();
    String app = runfiles.rlocation(System.getProperty("macro.app"));
    ProcessBuilder builder = new ProcessBuilder(app).redirectErrorStream(true);
    builder.environment().putAll(runfiles.getEnvVars());
    Process process = builder.start();
    try {
      assertTrue(process.waitFor(30, TimeUnit.SECONDS), "Application did not finish");
      String output = new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
      assertEquals(0, process.exitValue(), output);
      assertTrue(output.contains("inline application OK"), output);
    } finally {
      process.destroyForcibly();
    }
  }

  @Test
  @Tag("skipped")
  void excludedTagMustNotRun() {
    fail("JUnit tag filters were not forwarded");
  }
}
