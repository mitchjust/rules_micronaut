package com.example.dataazurecosmos;

import io.micronaut.data.cosmos.annotation.CosmosRepository;
import io.micronaut.data.repository.CrudRepository;
import java.util.Optional;

@CosmosRepository
public interface BookRepository extends CrudRepository<Book, String> {
  Optional<Book> findByTitle(String title);
}
