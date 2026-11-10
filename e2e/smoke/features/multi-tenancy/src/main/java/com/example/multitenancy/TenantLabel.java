package com.example.multitenancy;

import io.micronaut.context.annotation.Value;
import jakarta.inject.Singleton;

@Singleton
public class TenantLabel {
  private final String tenantId;

  // Compiling tenantId requires the multitenancy expression visitor.
  public TenantLabel(@Value("#{ tenantId }") String tenantId) {
    this.tenantId = tenantId;
  }

  public String getTenantId() {
    return tenantId;
  }
}
