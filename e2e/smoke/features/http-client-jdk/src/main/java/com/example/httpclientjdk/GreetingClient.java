package com.example.httpclientjdk;

import io.micronaut.http.annotation.Get;
import io.micronaut.http.client.annotation.Client;

@Client("/")
public interface GreetingClient {
  @Get(value = "/greet/{name}", consumes = "text/plain")
  String greet(String name);
}
