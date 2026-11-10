package com.example.security;

import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.security.annotation.Secured;
import io.micronaut.security.rules.SecurityRule;
import jakarta.annotation.security.DenyAll;
import jakarta.annotation.security.PermitAll;
import jakarta.annotation.security.RolesAllowed;
import java.security.Principal;

@Controller("/account")
@Secured(SecurityRule.IS_AUTHENTICATED)
public class AccountController {
  @Get(produces = "text/plain")
  public String whoami(Principal principal) {
    return principal.getName();
  }

  @Get(value = "/public", produces = "text/plain")
  @Secured(SecurityRule.IS_ANONYMOUS)
  public String open() {
    return "anyone";
  }

  @Get(value = "/jakarta-public", produces = "text/plain")
  @PermitAll
  public String jakartaPublic() {
    return "anyone";
  }

  @Get(value = "/jakarta-reader", produces = "text/plain")
  @RolesAllowed("reader")
  public String jakartaReader() {
    return "readable";
  }

  @Get(value = "/jakarta-denied", produces = "text/plain")
  @DenyAll
  public String jakartaDenied() {
    return "must never be returned";
  }
}
