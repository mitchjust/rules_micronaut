package com.example.openapi;

import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;

@Controller("/hello")
public class GreetingController {
  @Get(produces = "text/plain")
  public String hello() {
    return "hello";
  }
}
