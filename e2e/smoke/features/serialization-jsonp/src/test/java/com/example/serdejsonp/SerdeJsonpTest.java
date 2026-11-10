package com.example.serdejsonp;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.example.serialization.SharedMessage;
import io.micronaut.serde.ObjectMapper;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import jakarta.json.spi.JsonProvider;
import org.junit.jupiter.api.Test;

@MicronautTest(startApplication = false)
class SerdeJsonpTest {
  @Inject ObjectMapper mapper;

  @Test
  void roundTripsWithSelectedImplementation() throws Exception {
    var original = new Message("Ada", 42);
    var bytes = mapper.writeValueAsBytes(original);
    assertEquals(original, mapper.readValue(bytes, Message.class));
    assertEquals("io.micronaut.serde.json.stream.JsonStreamMapper", mapper.getClass().getName());
    String expectedProvider = System.getProperty("jsonp.provider");
    assertEquals(expectedProvider, JsonProvider.provider().getClass().getName());
    String otherProvider = expectedProvider.startsWith("org.eclipse.parsson.")
        ? "org.apache.johnzon.core.JsonProviderImpl"
        : "org.eclipse.parsson.JsonProviderImpl";
    assertThrows(ClassNotFoundException.class, () -> Class.forName(otherProvider));
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
