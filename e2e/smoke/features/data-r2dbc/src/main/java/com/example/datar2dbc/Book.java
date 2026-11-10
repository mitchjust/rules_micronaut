package com.example.datar2dbc;

import io.micronaut.data.annotation.GeneratedValue;
import io.micronaut.data.annotation.Id;
import io.micronaut.data.annotation.MappedEntity;

@MappedEntity
public record Book(@Id @GeneratedValue Long id, String title) {}
