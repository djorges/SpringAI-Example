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
import java.time.Duration;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Stream;


@Slf4j
@Service
public class AIService {

    private final ChatClient anthropicChatClient;
    private final ChatClient openAIChatClient;
    private final ChatClient ollamaChatClient;
    private final Resource systemPrompt;
    private final Executor aiExecutor;
    private final AIHistoryService aiHistoryService;
    private static final Duration AI_TIMEOUT = Duration.ofSeconds(60);

    public AIService(
            @Qualifier("anthropicChatClient") ChatClient anthropicChatClient,
            @Qualifier("openAIChatClient") ChatClient openAIChatClient,
            @Qualifier("ollamaChatClient") ChatClient ollamaChatClient,
            @Value("classpath:static/system-prompt-v1.st") Resource systemPrompt,
            @Qualifier("aiExecutor") Executor aiExecutor,
            AIHistoryService aiHistoryService
    ) {

        this.anthropicChatClient = anthropicChatClient;
        this.openAIChatClient = openAIChatClient;
        this.ollamaChatClient = ollamaChatClient;
        this.systemPrompt = systemPrompt;
        this.aiExecutor = aiExecutor;
        this.aiHistoryService = aiHistoryService;
    }

    public AIResponse generateResponses(String prompt) {

        validatePrompt(prompt);

        CompletableFuture<String> anthropicFuture =
                generateAsync(anthropicChatClient, prompt);

        CompletableFuture<String> openAIFuture =
                generateAsync(openAIChatClient, prompt);

        CompletableFuture<String> ollamaFuture =
                generateAsync(ollamaChatClient, prompt);

        return AIResponse.builder()
                .anthropic(getResult(anthropicFuture))
                .openAI(getResult(openAIFuture))
                .ollama(getResult(ollamaFuture))
                .build();
    }

    // 1. Ejecutar proveedor de forma asincrónica
    private CompletableFuture<String> generateAsync(
            ChatClient client,
            String prompt) {

        return CompletableFuture.supplyAsync(
                () -> generateSingleResponse(client, prompt),
                aiExecutor
        ).orTimeout(AI_TIMEOUT.toSeconds(), TimeUnit.SECONDS)
         .exceptionally(ex -> {
             log.error("Error generating AI response: {}", ex.getMessage());
             return null;
         });
    }

    // 2. Generar respuesta individual
    public String generateSingleResponse(
            ChatClient client,
            String prompt) {

        try {
            String response = client.prompt()
                    .system(systemPrompt)
                    .user(prompt)
                    .call()
                    .content();

            return validateResponse(response) ? response : null;

        } catch (Exception e) {
            log.error("AI provider failed: {}", e.getMessage());
            return null;
        }
    }

    // 3. Obtener resultado sin interrumpir las demás respuestas
    private String getResult(CompletableFuture<String> future) {

        try {
            return future.join();
        } catch (CompletionException e) {
            log.error("Could not retrieve AI result", e);
            return null;
        }
    }

    // 4. Validar prompt
    public void validatePrompt(String prompt) {

        if (prompt == null || prompt.isBlank()) {
            throw new IllegalArgumentException(
                    "Prompt cannot be empty"
            );
        }

        if (prompt.length() > 10000) {
            throw new IllegalArgumentException(
                    "Prompt exceeds maximum length"
            );
        }
    }

    // 5. Validar respuesta
    public boolean validateResponse(String response) {
        return response != null && !response.isBlank();
    }

    // 6. Contar respuestas exitosas
    public int countSuccessfulResponses(AIResponse response) {

        return (int) Stream.of(
                response.getAnthropic(),
                response.getOpenAI(),
                response.getOllama()
        ).filter(this::validateResponse).count();
    }

    // 7. Obtener respuestas disponibles
    public Map<String, String> getAvailableResponses(AIResponse response) {

        Map<String, String> responses = new LinkedHashMap<>();

        if (validateResponse(response.getAnthropic())) {
            responses.put("Anthropic", response.getAnthropic());
        }

        if (validateResponse(response.getOpenAI())) {
            responses.put("OpenAI", response.getOpenAI());
        }

        if (validateResponse(response.getOllama())) {
            responses.put("Ollama", response.getOllama());
        }

        return responses;
    }

    public AIResponse generateAndSave(
        Long conversationId,
        String prompt) {

    AIResponse response = generateResponses(prompt);

    aiHistoryService.saveExecution(
            conversationId,
            prompt,
            response
    );

    return response;
    }
}
