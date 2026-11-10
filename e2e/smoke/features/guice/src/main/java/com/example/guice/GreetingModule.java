package com.example.guice;

import com.google.inject.AbstractModule;

public class GreetingModule extends AbstractModule {
  @Override
  protected void configure() {
    bind(Greeting.class).toInstance(() -> "hello guice");
  }
}
