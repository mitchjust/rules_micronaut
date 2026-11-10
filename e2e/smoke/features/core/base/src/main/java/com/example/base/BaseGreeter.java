package com.example.base;

import jakarta.inject.Inject;

/**
 * A superclass in a different Bazel target from its subclasses. Bazel compiles
 * dependants against a header jar, which normally drops private members and
 * would make Micronaut silently skip the private field below. micronaut_library
 * keeps private fields in the header jar, so it is injected.
 */
public abstract class BaseGreeter {
  @Inject private Punctuation punctuation;

  protected String exclaim(String s) {
    if (punctuation == null) {
      throw new IllegalStateException("private @Inject field in superclass was not injected");
    }
    return punctuation.exclaim(s);
  }
}
