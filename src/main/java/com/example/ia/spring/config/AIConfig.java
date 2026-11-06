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
public class AIConfiguration{

    @Bean("anthropicChatClient")
    public ChatClient anthropicChatClient(
            @Qualifier("anthropicChatModel") ChatModel chatModelAnthropic
    ){
        return ChatClient.builder(chatModelAnthropic)
                .defaultOptions(
                        AnthropicChatOptions.builder().temperature(1.0).model(Model.CLAUDE_OPUS_4_5)
                )
                .build();
    }

    @Bean("openAIChatClient")
    public ChatClient openAIChatClient(
            @Qualifier("openAIChatModel") ChatModel chatModel
    ){
        return ChatClient.builder(chatModel)
                .defaultOptions(
                    OpenAiChatOptions.builder().model(com.openai.models.ChatModel.GPT_40_MINI.asString())
                )
                .build();
    }

}
