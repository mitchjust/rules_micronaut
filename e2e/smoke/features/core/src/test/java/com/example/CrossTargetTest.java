package com.example;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.example.base.Person;
import com.example.base.PersonBuilder;
import com.example.plain.Clock;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

/** Generated types and beans from other targets. */
@MicronautTest(startApplication = false)
class CrossTargetTest {
  @Inject Clock clock;

  @Test
  void usesBuilderGeneratedInAnotherTarget() {
    Person person = PersonBuilder.builder().name("Ada").age(36).build();
    assertEquals(new Person("Ada", 36), person);
  }

  @Test
  void injectsBeanFromPlainJavaLibrary() {
    assertEquals("now", clock.now());
  }
}
