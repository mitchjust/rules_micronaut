package com.example;

import io.micronaut.context.annotation.ConfigurationProperties;

/** Bound from the `greeting` prefix in application.yml (and application-<env>.yml). */
@ConfigurationProperties("greeting")
public record GreetingConfig(String salutation, int repeat) {}
