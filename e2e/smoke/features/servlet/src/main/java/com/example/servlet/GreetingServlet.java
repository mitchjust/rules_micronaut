package com.example.servlet;

import jakarta.inject.Inject;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet("/hello")
public class GreetingServlet extends HttpServlet {
  @Inject Greeting greeting;

  @Override
  protected void doGet(HttpServletRequest request, HttpServletResponse response)
      throws IOException {
    response.setContentType("text/plain");
    response.getWriter().write(greeting.hello(request.getParameter("name")));
  }
}
