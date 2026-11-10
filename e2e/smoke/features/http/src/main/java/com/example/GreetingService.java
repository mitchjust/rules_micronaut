package com.example;

import jakarta.inject.Singleton;

@Singleton
public class GreetingService {
  public Greeting greet(String name) {
    return new Greeting("Hello, " + name + "!");
  }
}
