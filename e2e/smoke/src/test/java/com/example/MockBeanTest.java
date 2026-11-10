package com.example;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.micronaut.http.client.HttpClient;
import io.micronaut.http.client.annotation.Client;
import io.micronaut.test.annotation.MockBean;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

@MicronautTest
class MockBeanTest {
  @Inject
  @Client("/")
  HttpClient client;

  @Inject GreetingService greetingService;

  @MockBean(GreetingService.class)
  GreetingService mockGreetingService() {
    return mock(GreetingService.class);
  }

  @Test
  void controllerUsesTheMock() {
    when(greetingService.greet("bazel")).thenReturn(new Greeting("mocked"));
    assertEquals("mocked", client.toBlocking().retrieve("/hello/bazel", Greeting.class).message());
    verify(greetingService).greet("bazel");
  }
}
