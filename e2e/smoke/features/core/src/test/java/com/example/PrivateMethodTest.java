package com.example;

import static org.junit.jupiter.api.Assertions.assertTrue;

import com.example.base.BaseLifecycle;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.junit.jupiter.api.Test;

/**
 * A private lifecycle method inherited from another Bazel target, through micronaut_full_jar_deps.
 */
@MicronautTest(startApplication = false)
class PrivateMethodTest {
  @Singleton
  static class Service extends BaseLifecycle {}

  @Inject Service service;

  @Test
  void callsPrivateLifecycleMethodInheritedFromAnotherTarget() {
    assertTrue(service.started());
  }
}
