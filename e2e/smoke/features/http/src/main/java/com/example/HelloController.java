package com.example;

import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;

@Controller("/hello")
public class HelloController {
  private final GreetingService greetingService;

  HelloController(GreetingService greetingService) {
    this.greetingService = greetingService;
  }

  // Binding `name` by parameter name relies on javac -parameters.
  @Get("/{name}")
  public Greeting hello(String name) {
    return greetingService.greet(name);
  }
}
