package com.example.rabbitmq;

import com.rabbitmq.client.Channel;
import io.micronaut.rabbitmq.connect.ChannelInitializer;
import jakarta.inject.Singleton;
import java.io.IOException;

@Singleton
public class BookQueue extends ChannelInitializer {
  @Override
  public void initialize(Channel channel, String name) throws IOException {
    channel.queueDeclare("books", false, false, false, null);
  }
}
