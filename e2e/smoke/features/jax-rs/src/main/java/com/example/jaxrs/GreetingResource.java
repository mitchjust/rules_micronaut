package com.example.jaxrs;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;

@Path("/hello")
public class GreetingResource {
  @GET
  @Path("/{name}")
  @Produces("text/plain")
  public String hello(@PathParam("name") String name) {
    return "hello " + name;
  }
}
