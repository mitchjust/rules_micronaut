package com.example.serialization;

import io.micronaut.serde.annotation.Serdeable;

@Serdeable
public record SharedMessage(String name, int count) {}
