package com.example.data;

import io.micronaut.data.jdbc.annotation.JdbcRepository;
import io.micronaut.data.model.query.builder.sql.Dialect;
import io.micronaut.data.repository.CrudRepository;
import java.util.List;

@JdbcRepository(dialect = Dialect.H2)
public interface BookRepository extends CrudRepository<Book, Long> {
  // Query derived from the method name at compile time by micronaut-data-processor.
  List<Book> findByPagesGreaterThan(int pages);
}
