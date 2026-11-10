package com.example.jsonschema;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;

class JsonSchemaTest {
  @Test
  void packagesGeneratedSchema() throws Exception {
    try (var input = Person.class.getResourceAsStream("/META-INF/schemas/person.schema.json")) {
      assertNotNull(input, "schema generated in the model library");
      var schema = new String(input.readAllBytes(), StandardCharsets.UTF_8);
      assertTrue(schema.contains("\"name\""), schema);
      assertTrue(schema.contains("\"age\""), schema);
      assertTrue(schema.contains("\"integer\""), schema);
      assertTrue(schema.contains("\"string\""), schema);
    }
  }
}
