package com.example.datamongodb;

import io.micronaut.data.mongodb.annotation.MongoRepository;
import io.micronaut.data.repository.CrudRepository;
import java.util.Optional;

@MongoRepository
public interface BookRepository extends CrudRepository<Book, String> {
  Optional<Book> findByTitle(String title);
}
