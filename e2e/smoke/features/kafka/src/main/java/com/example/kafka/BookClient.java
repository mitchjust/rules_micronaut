package com.example.kafka;

import io.micronaut.configuration.kafka.annotation.KafkaClient;
import io.micronaut.configuration.kafka.annotation.KafkaKey;
import io.micronaut.configuration.kafka.annotation.Topic;
import io.micronaut.messaging.annotation.MessageBody;
import java.util.concurrent.CompletableFuture;
import org.apache.kafka.clients.producer.RecordMetadata;

@KafkaClient
public interface BookClient {
  @Topic("books")
  CompletableFuture<RecordMetadata> send(@KafkaKey String id, @MessageBody String title);
}
