package com.example.springboot;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("greeting")
public class GreetingProperties {
  private String message;

  public String getMessage() {
    return message;
  }

  public void setMessage(String message) {
    this.message = message;
  }
}
