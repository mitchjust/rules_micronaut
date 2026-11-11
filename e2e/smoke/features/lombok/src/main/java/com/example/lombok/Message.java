package com.example.lombok;

import io.micronaut.serde.annotation.Serdeable;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Serdeable
public class Message {
  private String text;

  @Builder
  public Message(String text) {
    this.text = text;
  }
}
