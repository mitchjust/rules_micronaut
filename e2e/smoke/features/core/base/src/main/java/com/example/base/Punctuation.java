package com.example.base;

import jakarta.inject.Singleton;

@Singleton
public class Punctuation {
  public String exclaim(String s) {
    return s + "!";
  }
}
