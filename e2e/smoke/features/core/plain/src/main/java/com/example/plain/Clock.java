package com.example.plain;

import jakarta.inject.Singleton;

@Singleton
public class Clock {
  public String now() {
    return "now";
  }
}
