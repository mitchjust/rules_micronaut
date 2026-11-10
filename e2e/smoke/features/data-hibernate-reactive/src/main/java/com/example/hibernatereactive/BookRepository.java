package com.example.hibernatereactive;

import io.micronaut.data.annotation.Repository;
import io.micronaut.data.repository.reactive.ReactorCrudRepository;
import reactor.core.publisher.Mono;

@Repository
public interface BookRepository extends ReactorCrudRepository<Book, Long> {
  Mono<Book> findByTitle(String title);
}
