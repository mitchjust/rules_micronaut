package com.example.rabbitmq;

import io.micronaut.rabbitmq.annotation.Binding;
import io.micronaut.rabbitmq.annotation.RabbitClient;

@RabbitClient
public interface BookClient {
  @Binding("books")
  void send(String title);
}
