package com.example;

import io.micronaut.serde.annotation.Serdeable;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Serdeable
public record BookRequest(@NotBlank @Size(max = 255) String title) {}
