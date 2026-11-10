package com.example.sourcegen;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class SourcegenTest {
  @Test
  void consumesBuilderFromAnotherLibrary() {
    assertEquals(new Person("Ada", 36), PersonBuilder.builder().name("Ada").age(36).build());
  }
}
