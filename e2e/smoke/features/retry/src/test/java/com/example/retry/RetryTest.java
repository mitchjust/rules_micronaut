package com.example.retry;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import io.micronaut.retry.exception.CircuitOpenException;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

@MicronautTest(startApplication = false)
class RetryTest {
  @Inject UnstableService service;
  @Inject Greeting greeting;

  @Test
  void invokesFallbackWhenThePrimaryBeanFails() {
    assertEquals("Hello, Ada", greeting.greet("Ada"));
  }

  @Test
  void retriesTransientFailuresThroughGeneratedAdvice() {
    assertEquals("recovered", service.eventuallySucceeds());
    assertEquals(3, service.transientAttempts());
  }

  @Test
  void propagatesFailureAfterExhaustingRetries() {
    var failure = assertThrows(IllegalStateException.class, service::alwaysFails);
    assertEquals("Unavailable", failure.getMessage());
    assertEquals(3, service.permanentAttempts());
  }

  @Test
  void openCircuitRejectsCallsWithoutInvokingTheService() {
    assertThrows(IllegalStateException.class, service::circuitProtected);
    assertEquals(2, service.circuitAttempts());
    assertThrows(CircuitOpenException.class, service::circuitProtected);
    assertEquals(2, service.circuitAttempts());
  }
}
