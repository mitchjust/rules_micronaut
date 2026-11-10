package com.example.base;

import io.micronaut.sourcegen.annotations.Builder;

/** micronaut-sourcegen generates PersonBuilder, which //features/core:CrossTargetTest uses from another target. */
@Builder
public record Person(String name, int age) {}
