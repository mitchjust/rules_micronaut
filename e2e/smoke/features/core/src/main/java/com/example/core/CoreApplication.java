package com.example.core;

import io.micronaut.context.ApplicationContext;
import jakarta.inject.Singleton;

public class CoreApplication {
  @Singleton
  public static class Greeting {
    public String message() {
      return "core only";
    }
  }

  public static void main(String[] args) throws Exception {
    // Other targets in this module use these features. They must not leak here.
    for (String name :
        new String[] {
          "io.micronaut.serde.ObjectMapper",
          "com.zaxxer.hikari.HikariDataSource",
          "io.micronaut.security.annotation.Secured",
          "org.yaml.snakeyaml.Yaml",
          "ch.qos.logback.classic.LoggerContext",
          "io.micronaut.annotation.processing.BeanDefinitionInjectProcessor"
        }) {
      try {
        Class.forName(name);
        throw new AssertionError("Unexpected runtime dependency: " + name);
      } catch (ClassNotFoundException expected) {
        // This application selects only core support.
      }
    }
    try (var context = ApplicationContext.run()) {
      System.out.println(context.getBean(Greeting.class).message());
    }
  }
}
