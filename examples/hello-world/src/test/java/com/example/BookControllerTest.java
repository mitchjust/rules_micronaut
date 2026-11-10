package com.example;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import io.micronaut.context.annotation.Property;
import io.micronaut.core.type.Argument;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.MediaType;
import io.micronaut.http.client.HttpClient;
import io.micronaut.http.client.annotation.Client;
import io.micronaut.http.client.exceptions.HttpClientResponseException;
import io.micronaut.serde.annotation.Serdeable;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@MicronautTest(transactional = false)
@Property(name = "micronaut.server.port", value = "-1")
@Property(name = "datasources.default.url", value = "jdbc:h2:mem:books;DB_CLOSE_DELAY=-1")
@Property(name = "datasources.default.driver-class-name", value = "org.h2.Driver")
@Property(name = "datasources.default.username", value = "sa")
@Property(name = "datasources.default.password", value = "")
@Property(name = "jpa.default.properties.hibernate.hbm2ddl.auto", value = "create-drop")
class BookControllerTest {
  @Inject
  @Client("/")
  HttpClient client;

  @Inject BookRepository books;

  @BeforeEach
  void clearBooks() {
    books.deleteAll();
  }

  @Test
  void createsReadsUpdatesAndDeletesBooks() {
    var created =
        client.toBlocking()
            .exchange(HttpRequest.POST("/books", new BookRequest("Dune")), BookResponse.class);
    assertEquals(HttpStatus.CREATED, created.status());
    BookResponse book = created.body();
    assertNotNull(book);
    assertNotNull(book.id());
    assertEquals("Dune", book.title());
    String path = "/books/" + book.id();
    assertEquals(path, created.getHeaders().get("Location"));
    assertEquals(book, client.toBlocking().retrieve(path, BookResponse.class));
    assertEquals(List.of(book), listBooks());

    var updated =
        client.toBlocking()
            .exchange(HttpRequest.PUT(path, new BookRequest("Dune Messiah")), BookResponse.class);
    assertEquals(HttpStatus.OK, updated.status());
    BookResponse expected = new BookResponse(book.id(), "Dune Messiah");
    assertEquals(expected, updated.body());
    assertEquals(expected, client.toBlocking().retrieve(path, BookResponse.class));

    assertEquals(
        HttpStatus.NO_CONTENT, client.toBlocking().exchange(HttpRequest.DELETE(path)).status());
    assertFailure(HttpStatus.NOT_FOUND, HttpRequest.GET(path));
    assertFailure(HttpStatus.NOT_FOUND, HttpRequest.PUT(path, new BookRequest("Missing")));
    assertFailure(HttpStatus.NOT_FOUND, HttpRequest.DELETE(path));
    assertEquals(List.of(), listBooks());
  }

  @Test
  void validatesTitlesBeforeCreatingOrUpdatingBooks() {
    String title = "x".repeat(255);
    BookResponse created =
        client.toBlocking()
            .retrieve(HttpRequest.POST("/books", new BookRequest(title)), BookResponse.class);
    assertEquals(title, created.title());
    String path = "/books/" + created.id();

    title = "y".repeat(255);
    BookResponse updated =
        client.toBlocking()
            .retrieve(HttpRequest.PUT(path, new BookRequest(title)), BookResponse.class);
    assertEquals(new BookResponse(created.id(), title), updated);

    for (String body :
        List.of(
            "{}",
            "{\"title\":null}",
            "{\"title\":\"\"}",
            "{\"title\":\"   \"}",
            "{\"title\":\"" + "x".repeat(256) + "\"}")) {
      assertFailure(
          HttpStatus.BAD_REQUEST,
          HttpRequest.POST("/books", body).contentType(MediaType.APPLICATION_JSON_TYPE));
      assertFailure(
          HttpStatus.BAD_REQUEST,
          HttpRequest.PUT(path, body).contentType(MediaType.APPLICATION_JSON_TYPE));
    }

    assertEquals(updated, client.toBlocking().retrieve(path, BookResponse.class));
    assertEquals(List.of(updated), listBooks());
  }

  private List<BookResponse> listBooks() {
    return client.toBlocking()
        .retrieve(HttpRequest.GET("/books"), Argument.listOf(BookResponse.class));
  }

  private void assertFailure(HttpStatus expected, HttpRequest<?> request) {
    HttpClientResponseException error =
        assertThrows(
            HttpClientResponseException.class,
            () -> client.toBlocking().exchange(request, String.class));
    assertEquals(expected, error.getStatus());
  }

  @Serdeable.Deserializable
  public record BookResponse(Long id, String title) {}
}
