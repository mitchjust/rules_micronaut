package com.example.macros;

import io.micronaut.context.BeanContext;
import jakarta.inject.Singleton;
import java.nio.charset.StandardCharsets;

@Singleton
public class InlineApplication {
  public String getMessage() {
    return "inline";
  }

  public static void main(String[] args) throws Exception {
    try (BeanContext context = BeanContext.run()) {
      check("inline", context.getBean(InlineApplication.class).getMessage());
      check("hello", context.getBean(Greeting.class).getMessage());
    }
    try (var resource = InlineApplication.class.getResourceAsStream("/sentinel.txt")) {
      check("sentinel", new String(resource.readAllBytes(), StandardCharsets.UTF_8).trim());
    }
    try (var resource = InlineApplication.class.getResourceAsStream("/library.txt")) {
      check("library", new String(resource.readAllBytes(), StandardCharsets.UTF_8).trim());
    }
    System.out.println("inline application OK");
  }

  private static void check(String expected, String actual) {
    if (!expected.equals(actual)) {
      throw new AssertionError("Expected " + expected + ", got " + actual);
    }
  }
}
