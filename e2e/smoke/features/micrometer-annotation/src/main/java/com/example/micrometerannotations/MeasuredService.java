package com.example.micrometerannotations;

import io.micrometer.core.annotation.Counted;
import io.micrometer.core.annotation.Timed;
import jakarta.inject.Singleton;

@Singleton
public class MeasuredService {
  @Counted("feature.calls")
  @Timed("feature.duration")
  public String work() {
    return "done";
  }
}
