package com.example.kafka;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import io.micronaut.test.support.TestPropertyProvider;
import jakarta.inject.Inject;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.kafka.KafkaContainer;

@Testcontainers
@MicronautTest(startApplication = false, transactional = false)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class KafkaTest implements TestPropertyProvider {
  @Container static KafkaContainer kafka = new KafkaContainer("apache/kafka:4.1.0");

  @Inject BookClient client;
  @Inject BookListener listener;

  @Override
  public Map<String, String> getProperties() {
    return Map.of("kafka.bootstrap.servers", kafka.getBootstrapServers());
  }

  @Test
  void roundTripsThroughGeneratedClientAndListener() throws Exception {
    var metadata = client.send("dune", "Dune").get(30, TimeUnit.SECONDS);
    var book = listener.awaitBook();

    assertNotNull(book, "The Kafka listener should receive the produced book");
    assertEquals("dune", book.key());
    assertEquals("Dune", book.value());
    assertEquals("books", book.topic());
    assertEquals(metadata.partition(), book.partition());
    assertEquals(metadata.offset(), book.offset());
  }
}
