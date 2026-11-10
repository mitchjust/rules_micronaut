package com.example.datamongodb;

import io.micronaut.serde.annotation.Serdeable;

@Serdeable
public record BookTitle(String title) {}
