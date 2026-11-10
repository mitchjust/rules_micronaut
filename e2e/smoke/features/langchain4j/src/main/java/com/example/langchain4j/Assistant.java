package com.example.langchain4j;

import io.micronaut.langchain4j.annotation.AiService;

@AiService
public interface Assistant {
  String chat(String message);
}
