package com.example.serialization;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

import io.micronaut.context.ApplicationContext;
import io.micronaut.core.beans.BeanIntrospection;
import io.micronaut.serde.ObjectMapper;
import org.junit.jupiter.api.Test;

class SerializationTest {
  @Test
  void generatesMetadataWithoutSelectingAMapper() {
    var introspection = BeanIntrospection.getIntrospection(SharedMessage.class);
    assertEquals(new SharedMessage("Ada", 42), introspection.instantiate("Ada", 42));
    try (var context = ApplicationContext.run()) {
      assertFalse(context.containsBean(ObjectMapper.class));
    }
    for (String implementation : new String[] {
      "io.micronaut.serde.jackson.JacksonJsonMapper",
      "io.micronaut.serde.bson.BsonBinaryMapper",
      "io.micronaut.serde.json.stream.JsonStreamMapper",
      "org.eclipse.parsson.JsonProviderImpl",
      "org.apache.johnzon.core.JsonProviderImpl"
    }) {
      assertThrows(ClassNotFoundException.class, () -> Class.forName(implementation));
    }
  }
}
