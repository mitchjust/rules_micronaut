package com.example.serde;

import io.micronaut.context.ApplicationContext;
import io.micronaut.serde.ObjectMapper;
import io.micronaut.serde.annotation.Serdeable;

public class SerdeApplication {
  @Serdeable
  public record Message(String message) {}

  public static void main(String[] args) throws Exception {
    try (var context = ApplicationContext.run()) {
      var mapper = context.getBean(ObjectMapper.class);
      String json = mapper.writeValueAsString(new Message("inline"));
      if (!new Message("inline").equals(mapper.readValue(json, Message.class))) {
        throw new AssertionError("Inline application did not generate serialization metadata");
      }
      System.out.println(json);
    }
  }
}
