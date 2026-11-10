package com.example.openapi.external;

import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;

@Controller("/external")
public class ExternalController {
  @Get(produces = "text/plain")
  public String get() {
    return "external";
  }
}
