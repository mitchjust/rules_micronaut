package com.example.spring;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class Consumer {
  @Autowired Greeting greeting;

  public String greet() {
    return greeting.hello();
  }
}
