package com.example;

import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import jakarta.validation.constraints.Size;

@Controller("/strict")
public class StrictController {
  // Enforced by micronaut-validation's processor-generated metadata: no @Validated needed.
  @Get("/{name}")
  public String hello(@Size(max = 5) String name) {
    return "Hi, " + name;
  }
}
