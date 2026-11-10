package com.example.langchain4j;

import dev.langchain4j.model.chat.ChatModel;
import io.micronaut.langchain4j.annotation.Lang4jConfig;

@Lang4jConfig(models = @Lang4jConfig.Model(kind = ChatModel.class, impl = EchoChatModel.class))
public class ModelConfiguration {}
