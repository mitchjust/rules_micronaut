package com.example.serdejsonp;

import io.micronaut.serde.annotation.Serdeable;

@Serdeable
public record Message(String name, int count) {}
