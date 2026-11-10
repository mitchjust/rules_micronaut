package com.example.datajpa;

import io.micronaut.data.annotation.Repository;
import io.micronaut.data.repository.CrudRepository;
import java.util.Optional;

@Repository
public interface BookRepository extends CrudRepository<Book, Long> {
  Optional<Book> findByTitle(String title);
}
