package com.example.datar2dbc;

import io.micronaut.data.model.query.builder.sql.Dialect;
import io.micronaut.data.r2dbc.annotation.R2dbcRepository;
import io.micronaut.data.repository.reactive.ReactorCrudRepository;
import reactor.core.publisher.Mono;

@R2dbcRepository(dialect = Dialect.H2)
public interface BookRepository extends ReactorCrudRepository<Book, Long> {
  Mono<Book> findByTitle(String title);
}
