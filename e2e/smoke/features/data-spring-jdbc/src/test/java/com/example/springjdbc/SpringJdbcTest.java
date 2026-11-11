package com.example.springjdbc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.micronaut.context.annotation.Property;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

@MicronautTest(startApplication = false, transactional = false)
@Property(name = "datasources.default.url", value = "jdbc:h2:mem:springjdbc;DB_CLOSE_DELAY=-1")
@Property(name = "datasources.default.driver-class-name", value = "org.h2.Driver")
@Property(name = "datasources.default.username", value = "sa")
@Property(name = "datasources.default.password", value = "")
@Property(name = "datasources.default.schema-generate", value = "CREATE_DROP")
@Property(name = "datasources.default.dialect", value = "H2")
@Property(name = "datasources.default.transaction-manager", value = "springJdbc")
class SpringJdbcTest {
  @Inject BookRepository repository;

  @Test
  void executesSpringRepositoryAgainstEmbeddedH2() {
    var book = new Book(1L, "Dune");
    assertEquals(book, repository.save(book));
    assertEquals(book, repository.findById(1L).orElseThrow());
    assertEquals(book, repository.findByTitle("Dune").orElseThrow());
    assertTrue(repository.findByTitle("missing").isEmpty());
    assertEquals(1L, repository.count());
    repository.deleteById(1L);
    assertEquals(0L, repository.count());
  }
}
