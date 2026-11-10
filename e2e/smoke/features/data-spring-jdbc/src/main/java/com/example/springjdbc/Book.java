package com.example.springjdbc;

import io.micronaut.data.annotation.Id;
import io.micronaut.data.annotation.MappedEntity;

@MappedEntity("books")
public record Book(@Id Long id, String title) {}
