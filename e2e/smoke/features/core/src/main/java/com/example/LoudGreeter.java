package com.example;

import com.example.base.BaseGreeter;
import jakarta.inject.Singleton;

@Singleton
public class LoudGreeter extends BaseGreeter {
  public String greet(String name) {
    return exclaim("HELLO, " + name.toUpperCase());
  }
}
