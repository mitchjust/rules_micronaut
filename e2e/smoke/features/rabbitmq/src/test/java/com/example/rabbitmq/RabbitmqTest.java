package com.example.rabbitmq;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import io.micronaut.test.support.TestPropertyProvider;
import jakarta.inject.Inject;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.rabbitmq.RabbitMQContainer;

@Testcontainers
@MicronautTest(startApplication = false, transactional = false)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class RabbitmqTest implements TestPropertyProvider {
  @Container static RabbitMQContainer rabbit = new RabbitMQContainer("rabbitmq:4.1.4-alpine");

  @Inject BookClient client;
  @Inject BookListener listener;

  @Override
  public Map<String, String> getProperties() {
    return Map.of("rabbitmq.uri", rabbit.getAmqpUrl());
  }

  @Test
  void deliversMessagesThroughGeneratedProducerAndListener() throws Exception {
    client.send("Dune");
    assertEquals("Dune", listener.awaitTitle());
    client.send("Dune Messiah");
    assertEquals("Dune Messiah", listener.awaitTitle());
  }
}
