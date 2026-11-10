package com.example.picocli;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import java.util.concurrent.Callable;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Singleton
@Command(name = "greet")
public class GreetingCommand implements Callable<Integer> {
  @Option(names = "--name", required = true)
  String name;

  @Inject Greeting greeting;

  public Integer call() {
    return greeting.length(name);
  }
}
