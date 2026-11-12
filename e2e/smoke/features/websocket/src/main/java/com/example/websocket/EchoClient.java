package com.example.websocket;

import io.micronaut.websocket.annotation.ClientWebSocket;
import io.micronaut.websocket.annotation.OnMessage;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

@ClientWebSocket
public abstract class EchoClient implements AutoCloseable {
  private final BlockingQueue<String> messages = new LinkedBlockingQueue<>();

  public abstract void send(String message);

  @Override
  public abstract void close();

  @OnMessage
  public void receive(String message) {
    messages.add(message);
  }

  public String awaitMessage() throws InterruptedException {
    return messages.poll(10, TimeUnit.SECONDS);
  }
}
