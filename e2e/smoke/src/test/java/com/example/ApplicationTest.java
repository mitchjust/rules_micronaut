package com.example;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.google.devtools.build.runfiles.Runfiles;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.junit.jupiter.api.Test;

/**
 * Runs the micronaut_application executable through Bazel runfiles. Checks that its launcher,
 * runtime dependencies, bean metadata, YAML configuration and Logback output work together.
 */
class ApplicationTest {
  // The prefix comes from the application's logback.xml console pattern.
  private static final Pattern SERVER_URL =
      Pattern.compile("^\\[smoke\\] .*Server Running: (http://\\S+)");

  @Test
  void applicationBinaryServesRequests() throws Exception {
    Runfiles runfiles = Runfiles.preload().unmapped();
    String binary = runfiles.rlocation(System.getProperty("application_binary"));
    ProcessBuilder builder = new ProcessBuilder(binary).redirectErrorStream(true);
    builder.environment().putAll(runfiles.getEnvVars());
    builder.environment().put("MICRONAUT_SERVER_PORT", "-1");
    Process app = builder.start();
    try {
      String baseUrl = awaitServerUrl(app).get(60, TimeUnit.SECONDS);
      assertNotNull(
          baseUrl, "Application must log its startup using the configured Logback format");
      try (HttpClient client = HttpClient.newHttpClient()) {
        HttpResponse<String> response =
            client.send(
                // The context path comes from application.yml, which requires SnakeYAML.
                HttpRequest.newBuilder(URI.create(baseUrl).resolve("/smoke/hello/binary"))
                    .timeout(Duration.ofSeconds(10))
                    .build(),
                HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());
        assertEquals("{\"message\":\"Hello, binary!\"}", response.body());
      }
    } finally {
      app.destroyForcibly();
      app.waitFor(10, TimeUnit.SECONDS);
    }
  }

  private static CompletableFuture<String> awaitServerUrl(Process app) {
    return CompletableFuture.supplyAsync(() -> {
      try (BufferedReader out =
          new BufferedReader(new InputStreamReader(app.getInputStream(), StandardCharsets.UTF_8))) {
        for (String line; (line = out.readLine()) != null; ) {
          System.out.println("[app] " + line);
          Matcher m = SERVER_URL.matcher(line);
          if (m.find()) {
            return m.group(1);
          }
        }
        return null;
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    });
  }
}
