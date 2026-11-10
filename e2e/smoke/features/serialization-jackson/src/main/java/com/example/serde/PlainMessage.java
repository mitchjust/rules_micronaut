package com.example.serde;

import io.micronaut.serde.annotation.Serdeable;

@Serdeable
public record PlainMessage(String message) {}
