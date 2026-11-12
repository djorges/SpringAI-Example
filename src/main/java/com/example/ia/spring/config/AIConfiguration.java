package com.example.ia.spring.config;

import com.anthropic.models.messages.Model;
import org.springframework.ai.anthropic.AnthropicChatOptions;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class AIConfiguration {

    @Bean("anthropicChatClient")
    public ChatClient anthropicChatClient(
            @Qualifier("anthropicChatModel") ChatModel chatModel
    ) {
        return ChatClient.builder(chatModel)
                .build();
    }

    @Bean("openAIChatClient")
    public ChatClient openAIChatClient(
            @Qualifier("openAiChatModel") ChatModel chatModel
    ) {
        return ChatClient.builder(chatModel)
                .build();
    }

    @Bean("ollamaChatClient")
    public ChatClient ollamaChatClient(
            @Qualifier("ollamaChatModel") ChatModel chatModel
    ) {
        return ChatClient.builder(chatModel)
                .build();
    }
}
