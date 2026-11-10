# Hello world with PostgreSQL

A Micronaut HTTP application with a greeting and a small books API backed by
PostgreSQL. It uses `data_jpa` for generated repositories, `serialization` for
JSON models, `validation` for request constraints, and `http` for controllers.
The application explicitly selects Hikari, the PostgreSQL driver, Jackson
serialization, and Logback.

Application configuration is in [application.yml](src/main/resources/application.yml).

## Run

Install Bazel and Docker with Compose. Bazel selects JDK 25 through `.bazelrc`.
From this directory, start PostgreSQL and wait for it to become healthy, then
start the application:

```sh
docker compose up -d --wait
bazel run //:app
```

PostgreSQL listens on `127.0.0.1:5433`, with database, username, and password all
set to `hello`. To use another database port, export `POSTGRES_PORT` before
running both commands. The application runs on port 8080.

Hibernate creates or updates the schema on startup for this local example.
The named Docker volume preserves data between runs. JPA calls run on
Micronaut's blocking executor.

## Try the endpoints

Run these commands in another terminal. The greeting endpoint is unchanged:

```sh
curl http://localhost:8080/hello
```

Create a book:

```sh
curl -i -X POST http://localhost:8080/books \
  -H 'Content-Type: application/json' \
  -d '{"title":"Dune"}'
```

The response is `201 Created`, with a generated ID and a `Location` header such
as `/books/1`. Use the returned ID in the remaining requests:

```sh
BOOK_ID=1

# List all books and fetch one.
curl http://localhost:8080/books
curl "http://localhost:8080/books/$BOOK_ID"

# Update its title.
curl -i -X PUT "http://localhost:8080/books/$BOOK_ID" \
  -H 'Content-Type: application/json' \
  -d '{"title":"Dune Messiah"}'

# Delete it.
curl -i -X DELETE "http://localhost:8080/books/$BOOK_ID"
```

| Method   | Path          | Success response                          |
| -------- | ------------- | ----------------------------------------- |
| `GET`    | `/hello`      | `200`, plain-text `Hello, world!`         |
| `POST`   | `/books`      | `201`, created book and `Location` header |
| `GET`    | `/books`      | `200`, JSON array of books                |
| `GET`    | `/books/{id}` | `200`, book                               |
| `PUT`    | `/books/{id}` | `200`, updated book                       |
| `DELETE` | `/books/{id}` | `204`, empty response                     |

Fetching, updating, or deleting a missing book returns `404`. Titles must be
nonblank and at most 255 characters. Micronaut validates `@Valid` request bodies
using `@NotBlank` and `@Size` on `BookRequest`; invalid requests return `400`.

## Stop or reset

Stop the application with Ctrl-C, then stop PostgreSQL:

```sh
docker compose down
```

To also delete the example's stored books and reset the database:

```sh
docker compose down -v
```

## Test

Run the HTTP tests with an in-memory H2 database:

```sh
bazel test //:BookControllerTest
```

The test starts Micronaut on a random port and overrides the PostgreSQL datasource
settings with H2, so Docker is not needed. It covers CRUD, missing books, and
validation of titles on both create and update, including the 255-character limit.

## Build and dependencies

To build a deploy jar:

```sh
bazel build //:app_deploy.jar
```

The module uses this repository's checkout through `local_path_override`. When
copying the example to another repository, remove that override and set the
`rules_micronaut` dependency to the release you want to use.

After changing Maven artifacts or the platform BOM, update the lockfiles:

```sh
REPIN=1 bazel run @maven//:pin
```
