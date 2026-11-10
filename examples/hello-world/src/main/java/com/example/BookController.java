package com.example;

import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Delete;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.Post;
import io.micronaut.http.annotation.Put;
import io.micronaut.http.annotation.Status;
import io.micronaut.http.exceptions.HttpStatusException;
import io.micronaut.scheduling.TaskExecutors;
import io.micronaut.scheduling.annotation.ExecuteOn;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;

@Controller("/books")
@ExecuteOn(TaskExecutors.BLOCKING)
public class BookController {
  private final BookRepository books;

  public BookController(BookRepository books) {
    this.books = books;
  }

  @Post
  public HttpResponse<Book> create(@Body @Valid BookRequest request) {
    Book book = books.save(new Book(request.title()));
    return HttpResponse.created(book, URI.create("/books/" + book.getId()));
  }

  @Get
  public List<Book> list() {
    return books.findAll();
  }

  @Get("/{id}")
  public Book get(Long id) {
    return find(id);
  }

  @Put("/{id}")
  public Book update(Long id, @Body @Valid BookRequest request) {
    Book book = find(id);
    book.setTitle(request.title());
    return books.update(book);
  }

  @Delete("/{id}")
  @Status(HttpStatus.NO_CONTENT)
  public void delete(Long id) {
    books.delete(find(id));
  }

  private Book find(Long id) {
    return books.findById(id)
        .orElseThrow(() -> new HttpStatusException(HttpStatus.NOT_FOUND, "Book not found"));
  }
}
