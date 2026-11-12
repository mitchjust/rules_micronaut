package com.example.httpclientjdk;

import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;

@Controller("/greet")
public class GreetingController {
  @Get(value = "/{name}", produces = "text/plain")
  public String greet(String name) {
    return "Hello, " + name;
  }
}
