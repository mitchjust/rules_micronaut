package com.example.rabbitmq;

import io.micronaut.rabbitmq.annotation.Queue;
import io.micronaut.rabbitmq.annotation.RabbitListener;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

@RabbitListener
public class BookListener {
  private final BlockingQueue<String> titles = new LinkedBlockingQueue<>();

  @Queue("books")
  public void receive(String title) {
    titles.add(title);
  }

  public String awaitTitle() throws InterruptedException {
    return titles.poll(30, TimeUnit.SECONDS);
  }
}
