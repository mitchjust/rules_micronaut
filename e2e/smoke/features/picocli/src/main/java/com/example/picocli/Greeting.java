package com.example.picocli;

import jakarta.inject.Singleton;

@Singleton
public class Greeting {
  public int length(String name) {
    return name.length();
  }
}
