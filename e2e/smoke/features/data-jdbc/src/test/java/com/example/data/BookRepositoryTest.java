package com.example.data;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import java.util.List;
import org.junit.jupiter.api.Test;

@MicronautTest(startApplication = false)
class BookRepositoryTest {
  @Inject BookRepository books;

  @Test
  void savesAndQueriesBooks() {
    Book saved = books.save(new Book(null, "Dune", 412));
    books.save(new Book(null, "Novella", 90));

    assertNotNull(saved.id());
    assertEquals(2, books.count());
    List<Book> long_ = books.findByPagesGreaterThan(100);
    assertEquals(List.of("Dune"), long_.stream().map(Book::title).toList());
  }
}
