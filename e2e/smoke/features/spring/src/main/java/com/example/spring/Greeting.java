package com.example.spring;

import org.springframework.stereotype.Component;

@Component
public class Greeting {
  public String hello() {
    return "hello";
  }
}
