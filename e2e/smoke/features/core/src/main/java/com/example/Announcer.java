package com.example;

import jakarta.inject.Singleton;

@Singleton
public class Announcer {
  private final GreetingConfig config;

  Announcer(GreetingConfig config) {
    this.config = config;
  }

  public String plain(String name) {
    return (config.salutation() + " " + name + " ").repeat(config.repeat()).trim();
  }

  // Intercepted: Micronaut generates an $Intercepted subclass at compile time.
  @Shout
  public String loud(String name) {
    return plain(name);
  }
}
