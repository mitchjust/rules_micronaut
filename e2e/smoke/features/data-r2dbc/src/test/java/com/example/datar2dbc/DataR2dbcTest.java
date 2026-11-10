package com.example.datar2dbc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import io.micronaut.context.annotation.Property;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import java.time.Duration;
import org.junit.jupiter.api.Test;

@MicronautTest(startApplication = false, transactional = false)
@Property(name = "r2dbc.datasources.default.url", value = "r2dbc:h2:mem:///r2dbc;DB_CLOSE_DELAY=-1")
@Property(name = "r2dbc.datasources.default.username", value = "sa")
@Property(name = "r2dbc.datasources.default.password", value = "")
@Property(name = "r2dbc.datasources.default.dialect", value = "H2")
@Property(name = "r2dbc.datasources.default.schema-generate", value = "CREATE_DROP")
class DataR2dbcTest {
  @Inject BookRepository repository;

  @Test
  void executesGeneratedReactiveRepositoryAgainstEmbeddedH2() {
    var saved = repository.save(new Book(null, "Dune")).block(Duration.ofSeconds(10));
    assertNotNull(saved);
    assertNotNull(saved.id());
    assertEquals(saved, repository.findByTitle("Dune").block(Duration.ofSeconds(10)));
    assertNull(repository.findByTitle("missing").block(Duration.ofSeconds(10)));
  }
}
