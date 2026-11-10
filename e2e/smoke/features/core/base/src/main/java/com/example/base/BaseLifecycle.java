package com.example.base;

import jakarta.annotation.PostConstruct;

/**
 * A superclass that keeps a private lifecycle method. Header jars omit private
 * methods, so targets that subclass it depend on this one through
 * micronaut_full_jar_deps.
 */
public abstract class BaseLifecycle {
  private boolean started;

  @SuppressWarnings("MicronautPrivateInjection")
  @PostConstruct
  private void start() {
    started = true;
  }

  public boolean started() {
    return started;
  }
}
