package com.example.multitenancy;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.micronaut.context.annotation.Property;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

@MicronautTest(startApplication = false)
@Property(name = "micronaut.multitenancy.tenantresolver.fixed.enabled", value = "true")
@Property(name = "micronaut.multitenancy.tenantresolver.fixed.tenant-id", value = "smoke-tenant")
class MultiTenancyTest {
  @Inject TenantLabel label;

  @Test
  void resolvesTenantInGeneratedExpression() {
    assertEquals("smoke-tenant", label.getTenantId());
  }
}
