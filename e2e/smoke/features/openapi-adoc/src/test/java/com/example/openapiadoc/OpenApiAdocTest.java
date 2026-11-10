package com.example.openapiadoc;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;

class OpenApiAdocTest {
  @Test
  void packagesGeneratedDocumentation() throws Exception {
    try (var input =
        ApiDefinition.class.getResourceAsStream("/META-INF/swagger/feature-api-1.adoc")) {
      assertNotNull(input, "generated resource from the annotated library");
      var document = new String(input.readAllBytes(), StandardCharsets.UTF_8);
      assertTrue(document.contains("/hello"), document);
      assertTrue(document.contains("feature-api"), document);
    }
  }

  @Test
  void excludesCompileOnlyAnnotationsAndProcessorsFromRuntime() {
    assertThrows(
        ClassNotFoundException.class,
        () -> Class.forName("io.swagger.v3.oas.annotations.OpenAPIDefinition"));
    assertThrows(
        ClassNotFoundException.class,
        () -> Class.forName("io.micronaut.openapi.visitor.OpenApiApplicationVisitor"));
  }
}
