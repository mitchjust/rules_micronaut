package com.example.servlet;

import jakarta.inject.Singleton;

@Singleton
public class Greeting {
  public String hello(String name) {
    return "hello " + name;
  }
}
