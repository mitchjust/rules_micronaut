package com.example.springweb;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class GreetingController {
  @GetMapping(value = "/hello/{name}", produces = "text/plain")
  public String hello(@PathVariable("name") String name) {
    return "hello " + name;
  }
}
