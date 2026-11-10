package com.example.macros;

import jakarta.inject.Singleton;

@Singleton
public class Greeting {
  public String getMessage() {
    return "hello";
  }
}
