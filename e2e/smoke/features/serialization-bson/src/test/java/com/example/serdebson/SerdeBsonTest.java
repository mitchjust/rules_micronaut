package com.example.serdebson;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.example.serialization.SharedMessage;
import io.micronaut.serde.bson.BsonBinaryMapper;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

@MicronautTest(startApplication = false)
class SerdeBsonTest {
  @Inject BsonBinaryMapper mapper;

  @Test
  void roundTripsWithSelectedImplementation() throws Exception {
    var original = new Message("Ada", 42);
    var bytes = mapper.writeValueAsBytes(original);
    assertEquals(original, mapper.readValue(bytes, Message.class));
    assertTrue(mapper.getClass().getName().contains(".bson."));
    assertThrows(
        ClassNotFoundException.class,
        () -> Class.forName("io.micronaut.serde.jackson.JacksonJsonMapper"));
  }

  @Test
  void roundTripsModelCompiledWithoutAMapper() throws Exception {
    var message = new SharedMessage("Ada", 42);
    assertEquals(message, mapper.readValue(mapper.writeValueAsBytes(message), SharedMessage.class));
  }
}
