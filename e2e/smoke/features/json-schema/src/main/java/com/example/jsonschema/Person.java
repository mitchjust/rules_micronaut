package com.example.jsonschema;

import io.micronaut.jsonschema.JsonSchema;

@JsonSchema(title = "Person", uri = "person")
public record Person(String name, int age) {}
