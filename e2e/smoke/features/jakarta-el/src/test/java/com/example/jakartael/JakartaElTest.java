package com.example.jakartael;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.micronaut.el.CompiledELContext;
import org.junit.jupiter.api.Test;

class JakartaElTest {
  @Test
  void evaluatesGeneratedExpression() {
    Object answer = Expressions$ELExpressions.ANSWER.getValue(new CompiledELContext());
    assertEquals(42L, answer);
  }
}
