package com.example.lombok;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import io.micronaut.context.ApplicationContext;
import io.micronaut.core.beans.BeanIntrospection;
import io.micronaut.serde.ObjectMapper;
import org.junit.jupiter.api.Test;

class LombokTest {
  @Test
  void exposesGeneratedMembersAcrossTargets() {
    Message.MessageBuilder builder = Message.builder();
    var message = builder.text("hello").build();
    assertEquals("hello", message.getText());
    message.setText("updated");
    assertEquals(new Message("updated"), message);
  }

  @Test
  void micronautIntrospectsGeneratedAccessors() {
    var introspection = BeanIntrospection.getIntrospection(Message.class);
    var property = introspection.getRequiredProperty("text", String.class);
    var message = introspection.instantiate();
    property.set(message, "hello");
    assertEquals("hello", property.get(message));
    assertEquals("hello", message.getText());
  }

  @Test
  void serializesThroughGeneratedAccessors() throws Exception {
    try (var context = ApplicationContext.run()) {
      var mapper = context.getBean(ObjectMapper.class);
      var message = Message.builder().text("hello").build();
      assertEquals("{\"text\":\"hello\"}", mapper.writeValueAsString(message));
      assertEquals(message, mapper.readValue("{\"text\":\"hello\"}", Message.class));
    }
  }

  @Test
  void lombokIsCompileOnly() {
    assertThrows(ClassNotFoundException.class, () -> Class.forName("lombok.Data"));
  }
}
