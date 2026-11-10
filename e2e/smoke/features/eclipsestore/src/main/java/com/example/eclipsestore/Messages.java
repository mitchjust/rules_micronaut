package com.example.eclipsestore;

import io.micronaut.eclipsestore.RootProvider;
import io.micronaut.eclipsestore.annotations.StoreRoot;
import jakarta.inject.Singleton;

@Singleton
public class Messages {
  private final RootProvider<Root> provider;

  public Messages(RootProvider<Root> provider) {
    this.provider = provider;
  }

  public String read() {
    return provider.root().message;
  }

  @StoreRoot
  public void write(String message) {
    provider.root().message = message;
  }
}
