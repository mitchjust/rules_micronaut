package com.example.langchain4j;

import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.request.ChatRequest;
import dev.langchain4j.model.chat.response.ChatResponse;

/** A deterministic local model: no network, credentials, or mocking framework. */
public class EchoChatModel implements ChatModel {
  private final String prefix;

  private EchoChatModel(String prefix) {
    this.prefix = prefix;
  }

  @Override
  public ChatResponse doChat(ChatRequest request) {
    var message = (UserMessage) request.messages().getLast();
    return ChatResponse.builder().aiMessage(AiMessage.from(prefix + message.singleText())).build();
  }

  public static Builder builder() {
    return new Builder();
  }

  public static class Builder {
    private String prefix = "hello ";

    public Builder prefix(String value) {
      prefix = value;
      return this;
    }

    public EchoChatModel build() {
      return new EchoChatModel(prefix);
    }
  }
}
