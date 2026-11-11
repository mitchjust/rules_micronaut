package com.example.dataazurecosmos;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.micronaut.json.JsonMapper;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import io.micronaut.test.support.TestPropertyProvider;
import jakarta.inject.Inject;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.testcontainers.containers.CosmosDBEmulatorContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

@Testcontainers
@MicronautTest(startApplication = false, transactional = false)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class CosmosTest implements TestPropertyProvider {
  @Container
  static CosmosDBEmulatorContainer cosmos =
      new CosmosDBEmulatorContainer(
              DockerImageName.parse(
                  "mcr.microsoft.com/cosmosdb/linux/azure-cosmos-emulator:vnext-EN20261008"))
          .withCommand("--protocol", "https", "--enable-explorer", "false")
          .withExposedPorts(8081, 8080)
          // The module's default log wait is for the legacy emulator.
          .waitingFor(Wait.forHttp("/ready").forPort(8080))
          .withStartupTimeout(Duration.ofMinutes(2));

  private final Map<String, String> previousSslProperties = new HashMap<>();
  private Path trustStore;

  @Inject BookRepository repository;
  @Inject JsonMapper mapper;

  @Override
  public Map<String, String> getProperties() {
    // Trust this container's certificate before Micronaut creates the Cosmos SDK client.
    try {
      trustStore = Files.createTempFile("cosmos-emulator-", ".p12");
      try (var output = Files.newOutputStream(trustStore)) {
        cosmos.buildNewKeyStore().store(output, cosmos.getEmulatorKey().toCharArray());
      }
      setSslProperty("javax.net.ssl.trustStore", trustStore.toString());
      setSslProperty("javax.net.ssl.trustStorePassword", cosmos.getEmulatorKey());
      setSslProperty("javax.net.ssl.trustStoreType", "PKCS12");
    } catch (Exception e) {
      throw new IllegalStateException("Could not trust the Cosmos emulator certificate", e);
    }
    return Map.of(
        "azure.cosmos.endpoint", cosmos.getEmulatorEndpoint(),
        "azure.cosmos.key", cosmos.getEmulatorKey(),
        "azure.cosmos.default-gateway-mode", "true",
        "azure.cosmos.endpoint-discovery-enabled", "false",
        "azure.cosmos.consistency-level", "SESSION",
        "azure.cosmos.database.database-name", "smoke",
        "azure.cosmos.database.update-policy", "CREATE_IF_NOT_EXISTS",
        "azure.cosmos.database.packages", "com.example.dataazurecosmos");
  }

  private void setSslProperty(String name, String value) {
    previousSslProperties.put(name, System.getProperty(name));
    System.setProperty(name, value);
  }

  @AfterAll
  void restoreTrustStore() throws Exception {
    previousSslProperties.forEach(
        (name, value) -> {
          if (value == null) {
            System.clearProperty(name);
          } else {
            System.setProperty(name, value);
          }
        });
    if (trustStore != null) {
      Files.deleteIfExists(trustStore);
    }
  }

  @Test
  void roundTripsThroughGeneratedRepository() {
    var book = new Book("dune", "Dune");
    assertEquals(book, repository.save(book));
    assertEquals(book, repository.findById("dune").orElseThrow());
    assertEquals(book, repository.findByTitle("Dune").orElseThrow());
    assertTrue(repository.findByTitle("missing").isEmpty());

    var updated = new Book("dune", "Dune Messiah");
    assertEquals(updated, repository.update(updated));
    assertEquals(updated, repository.findById("dune").orElseThrow());
    assertEquals(updated, repository.findByTitle("Dune Messiah").orElseThrow());

    repository.deleteById("dune");
    assertTrue(repository.findById("dune").isEmpty());
  }

  @Test
  void roundTripsDtoWithGeneratedSerde() throws Exception {
    var title = new BookTitle("Dune");
    assertEquals(title, mapper.readValue(mapper.writeValueAsBytes(title), BookTitle.class));
  }
}
