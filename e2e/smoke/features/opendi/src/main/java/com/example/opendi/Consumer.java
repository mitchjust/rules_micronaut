package com.example.opendi;

import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;
import jakarta.inject.Named;

@Dependent
public class Consumer {
  private final String message;

  @Inject
  public Consumer(@Named("message") String message) {
    this.message = message;
  }

  public String greet() {
    return message;
  }
}
