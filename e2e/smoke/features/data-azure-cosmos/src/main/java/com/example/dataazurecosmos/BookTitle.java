package com.example.dataazurecosmos;

import io.micronaut.serde.annotation.Serdeable;

@Serdeable
public record BookTitle(String title) {}
