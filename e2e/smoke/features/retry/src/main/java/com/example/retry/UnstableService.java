package com.example.retry;

import io.micronaut.retry.annotation.CircuitBreaker;
import io.micronaut.retry.annotation.Retryable;
import jakarta.inject.Singleton;

@Singleton
public class UnstableService {
  private int transientAttempts;
  private int permanentAttempts;
  private int circuitAttempts;

  @Retryable(attempts = "2", delay = "1ms")
  public String eventuallySucceeds() {
    if (++transientAttempts < 3) {
      throw new IllegalStateException("Try again");
    }
    return "recovered";
  }

  @Retryable(attempts = "2", delay = "1ms")
  public String alwaysFails() {
    permanentAttempts++;
    throw new IllegalStateException("Unavailable");
  }

  @CircuitBreaker(attempts = "1", delay = "1ms", reset = "1h", throwWrappedException = true)
  public String circuitProtected() {
    circuitAttempts++;
    throw new IllegalStateException("Unavailable");
  }

  public int transientAttempts() {
    return transientAttempts;
  }

  public int permanentAttempts() {
    return permanentAttempts;
  }

  public int circuitAttempts() {
    return circuitAttempts;
  }
}
