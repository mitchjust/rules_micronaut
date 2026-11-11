package com.example.datamongodb;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.micronaut.serde.bson.BsonBinaryMapper;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import io.micronaut.test.support.TestPropertyProvider;
import jakarta.inject.Inject;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.mongodb.MongoDBContainer;

@Testcontainers
@MicronautTest(startApplication = false, transactional = false)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class MongoDbTest implements TestPropertyProvider {
  @Container
  static MongoDBContainer mongo = new MongoDBContainer("mongo:7.0.28").withReplicaSet();

  @Inject BookRepository repository;
  @Inject BsonBinaryMapper mapper;

  @Override
  public Map<String, String> getProperties() {
    return Map.of("mongodb.uri", mongo.getReplicaSetUrl("smoke"));
  }

  @Test
  void roundTripsThroughGeneratedRepository() {
    var book = new Book("dune", "Dune");
    assertEquals(book, repository.save(book));
    assertEquals(book, repository.findById("dune").orElseThrow());
    assertEquals(book, repository.findByTitle("Dune").orElseThrow());
    assertTrue(repository.findByTitle("missing").isEmpty());
    repository.deleteById("dune");
    assertTrue(repository.findById("dune").isEmpty());
  }

  @Test
  void roundTripsDtoWithGeneratedBsonSerde() throws Exception {
    var title = new BookTitle("Dune");
    assertEquals(title, mapper.readValue(mapper.writeValueAsBytes(title), BookTitle.class));
  }
}
