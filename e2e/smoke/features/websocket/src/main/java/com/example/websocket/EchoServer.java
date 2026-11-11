package com.example.websocket;

import io.micronaut.websocket.WebSocketSession;
import io.micronaut.websocket.annotation.OnMessage;
import io.micronaut.websocket.annotation.ServerWebSocket;
import org.reactivestreams.Publisher;

@ServerWebSocket("/echo")
public class EchoServer {
  @OnMessage
  public Publisher<String> echo(String message, WebSocketSession session) {
    return session.send("echo: " + message);
  }
}
