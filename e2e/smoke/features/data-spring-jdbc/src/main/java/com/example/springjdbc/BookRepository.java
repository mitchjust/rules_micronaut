package com.example.springjdbc;

import io.micronaut.data.jdbc.annotation.JdbcRepository;
import io.micronaut.data.model.query.builder.sql.Dialect;
import java.util.Optional;
import org.springframework.data.repository.CrudRepository;

@JdbcRepository(dialect = Dialect.H2)
public interface BookRepository extends CrudRepository<Book, Long> {
  Optional<Book> findByTitle(String title);
}
