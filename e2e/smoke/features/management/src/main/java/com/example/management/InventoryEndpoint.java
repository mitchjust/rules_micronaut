package com.example.management;

import io.micronaut.management.endpoint.annotation.Endpoint;
import io.micronaut.management.endpoint.annotation.Read;
import java.util.Map;

@Endpoint(id = "inventory", defaultSensitive = false)
public class InventoryEndpoint {
  @Read
  public Map<String, Object> inventory() {
    return Map.of("books", 3);
  }
}
