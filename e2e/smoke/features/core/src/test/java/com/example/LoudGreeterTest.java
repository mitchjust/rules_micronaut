package com.example;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

/** Beans and injection points spread across two Bazel targets. */
@MicronautTest(startApplication = false)
class LoudGreeterTest {
  // BaseGreeter, in //features/core/base, has a private @Inject field.
  @Inject LoudGreeter greeter;

  @Test
  void injectsPrivateFieldInheritedFromAnotherTarget() {
    assertEquals("HELLO, BAZEL!", greeter.greet("bazel"));
  }
}
