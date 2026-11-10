package com.example.httpclient;

import io.micronaut.http.annotation.Get;
import io.micronaut.http.client.annotation.Client;

@Client("/")
public interface GreetingClient {
  @Get("/hello/{name}")
  String greet(String name);
}
