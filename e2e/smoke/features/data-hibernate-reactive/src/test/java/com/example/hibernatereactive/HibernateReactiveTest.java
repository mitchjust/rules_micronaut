package com.example.hibernatereactive;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import io.micronaut.context.annotation.Property;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import io.micronaut.test.support.TestPropertyProvider;
import jakarta.inject.Inject;
import java.time.Duration;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

@Testcontainers
@MicronautTest(startApplication = false, transactional = false)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Property(name = "jpa.default.reactive", value = "true")
@Property(name = "jpa.default.entity-scan.packages", value = "com.example.hibernatereactive")
@Property(name = "jpa.default.properties.hibernate.hbm2ddl.auto", value = "create-drop")
class HibernateReactiveTest implements TestPropertyProvider {
  @Container static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:17-alpine");

  @Inject BookRepository repository;

  @Override
  public Map<String, String> getProperties() {
    return Map.of(
        "jpa.default.properties.hibernate.connection.url", postgres.getJdbcUrl(),
        "jpa.default.properties.hibernate.connection.username", postgres.getUsername(),
        "jpa.default.properties.hibernate.connection.password", postgres.getPassword());
  }

  @Test
  void roundTripsThroughGeneratedReactiveRepository() {
    var book = new Book();
    book.setId(1L);
    book.setTitle("Dune");
    var saved = repository.save(book).block(Duration.ofSeconds(10));
    assertNotNull(saved);
    assertEquals(1L, saved.getId());

    var found = repository.findById(1L).block(Duration.ofSeconds(10));
    assertNotNull(found);
    assertEquals("Dune", found.getTitle());
    var byTitle = repository.findByTitle("Dune").block(Duration.ofSeconds(10));
    assertNotNull(byTitle);
    assertEquals(1L, byTitle.getId());
    assertNull(repository.findByTitle("missing").block(Duration.ofSeconds(10)));

    repository.deleteById(1L).block(Duration.ofSeconds(10));
    assertNull(repository.findById(1L).block(Duration.ofSeconds(10)));
  }
}
