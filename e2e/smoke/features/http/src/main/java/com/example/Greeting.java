package com.example;

import io.micronaut.serde.annotation.Serdeable;

@Serdeable
public record Greeting(String message) {}
