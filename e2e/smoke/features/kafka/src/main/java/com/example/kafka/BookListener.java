package com.example.kafka;

import io.micronaut.configuration.kafka.annotation.KafkaListener;
import io.micronaut.configuration.kafka.annotation.OffsetReset;
import io.micronaut.configuration.kafka.annotation.Topic;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import org.apache.kafka.clients.consumer.ConsumerRecord;

@KafkaListener(groupId = "book-smoke-test", offsetReset = OffsetReset.EARLIEST)
public class BookListener {
  private final BlockingQueue<ConsumerRecord<String, String>> received = new LinkedBlockingQueue<>();

  @Topic("books")
  public void receive(ConsumerRecord<String, String> book) {
    received.add(book);
  }

  public ConsumerRecord<String, String> awaitBook() throws InterruptedException {
    return received.poll(30, TimeUnit.SECONDS);
  }
}
