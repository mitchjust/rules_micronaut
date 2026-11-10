package com.example.openapi;

import com.example.openapi.external.ExternalController;
import io.micronaut.openapi.annotation.OpenAPIInclude;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;

@OpenAPIDefinition(info = @Info(title = "feature-api", version = "1"))
@OpenAPIInclude(ExternalController.class)
public class ApiDefinition {}
