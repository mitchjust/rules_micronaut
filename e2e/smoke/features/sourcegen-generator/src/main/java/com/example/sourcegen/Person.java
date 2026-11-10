package com.example.sourcegen;

import io.micronaut.sourcegen.annotations.Builder;

@Builder
public record Person(String name, int age) {}
