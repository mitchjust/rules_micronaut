package com.example.securityjwt;

import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.security.annotation.Secured;
import java.security.Principal;

@Controller("/jwt-account")
@Secured("reader")
public class JwtController {
  @Get(produces = "text/plain")
  public String account(Principal principal) {
    return principal.getName();
  }
}
