package com.example.datajpa;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.micronaut.context.annotation.Property;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

@MicronautTest(startApplication = false, transactional = false)
@Property(name = "datasources.default.url", value = "jdbc:h2:mem:jpa;DB_CLOSE_DELAY=-1")
@Property(name = "datasources.default.driver-class-name", value = "org.h2.Driver")
@Property(name = "datasources.default.username", value = "sa")
@Property(name = "datasources.default.password", value = "")
@Property(name = "jpa.default.entity-scan.packages", value = "com.example.datajpa")
@Property(name = "jpa.default.properties.hibernate.hbm2ddl.auto", value = "create-drop")
class DataJpaTest {
  @Inject BookRepository repository;

  @Test
  void executesGeneratedRepositoryAgainstEmbeddedH2() {
    var saved = repository.save(new Book("Dune"));
    assertNotNull(saved.getId());
    assertEquals(saved.getId(), repository.findByTitle("Dune").orElseThrow().getId());
    assertTrue(repository.findByTitle("missing").isEmpty());
  }
}
