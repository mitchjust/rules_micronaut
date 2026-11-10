package com.example.opendi;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Named;

@ApplicationScoped
public class GreetingProducer {
  @Produces
  @Named("message")
  public String message() {
    return "hello CDI";
  }
}
