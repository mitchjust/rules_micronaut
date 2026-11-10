package com.example.jakartael;

import io.micronaut.el.annotation.ELExpression;

@ELExpression(value = "${21 * 2}", name = "ANSWER", expectedType = Long.class)
public class Expressions {}
