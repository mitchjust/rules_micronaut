package com.example.dataazurecosmos;

import io.micronaut.data.annotation.Id;
import io.micronaut.data.annotation.MappedEntity;

@MappedEntity("books")
public record Book(@Id String id, String title) {}
