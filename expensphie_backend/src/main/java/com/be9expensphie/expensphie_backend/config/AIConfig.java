package com.be9expensphie.expensphie_backend.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AIConfig {
    @Bean("geminiChatClient")
    public ChatClient geminiChatClient(
            @Qualifier("googleGenAiChatModel") ChatModel geminiModel) {
        return ChatClient.builder(geminiModel).build();
    }

    @Bean("openAiChatClient")
    public ChatClient openAiChatClient(
            @Qualifier("openAiChatModel") ChatModel openAiModel) {
        return ChatClient.builder(openAiModel).build();
    }

}
