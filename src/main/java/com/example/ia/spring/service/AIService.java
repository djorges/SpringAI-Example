package com.example.ia.spring.service;

import com.example.ia.spring.model.AIResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@Slf4j
@Service
public class AIService {

    private final ChatClient anthropicChatClient;
    private final ChatClient openAIChatClient;
    private final ChatClient ollamaChatClient;
    private final Resource systemPrompt;
    private final Executor aiExecutor;

    public AIService(
        @Qualifier("anthropicChatClient") ChatClient anthropicChatClient,
        @Qualifier("openAIChatClient") ChatClient openAIChatClient,
        @Qualifier("ollamaChatClient") ChatClient ollamaChatClient,
        @Value("classpath:static/system-prompt-v1.st")
        Resource systemPrompt,
        @Qualifier("aiExecutor") Executor aiExecutor) {

        this.anthropicChatClient = anthropicChatClient;
        this.openAIChatClient = openAIChatClient;
        this.ollamaChatClient = ollamaChatClient;
        this.systemPrompt = systemPrompt;
        this.aiExecutor = aiExecutor;
    }

    public AIResponse generateResponses(String prompt) {

        CompletableFuture<String> anthropicFuture =
                CompletableFuture.supplyAsync(
                        () -> anthropicChatClient.prompt()
                                .system(systemPrompt)
                                .user(prompt)
                                .call()
                                .content(),
                        aiExecutor
                );

        CompletableFuture<String> openAIFuture =
                CompletableFuture.supplyAsync(
                        () -> openAIChatClient.prompt()
                                .system(systemPrompt)
                                .user(prompt)
                                .call()
                                .content(),
                        aiExecutor
                );

        CompletableFuture<String> ollamaFuture =
                CompletableFuture.supplyAsync(
                        () -> ollamaChatClient.prompt()
                                .system(systemPrompt)
                                .user(prompt)
                                .call()
                                .content(),
                        aiExecutor
                );

        CompletableFuture.allOf(
                anthropicFuture,
                openAIFuture,
                ollamaFuture
        ).join();

        return AIResponse.builder()
                .anthropic(anthropicFuture.join())
                .openAI(openAIFuture.join())
                .ollama(ollamaFuture.join())
                .build();
    }
}