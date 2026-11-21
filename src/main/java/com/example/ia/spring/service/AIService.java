package com.example.ia.spring.service;

import com.example.ia.spring.model.AIResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Stream;


@Slf4j
@Service
public class AIService {
    private static final int MAX_RETRIES = 3;
    private static final int MAX_PROMPT_PREVIEW = 2000;
    private static final Pattern CONTROL_CHARACTERS =
        Pattern.compile("[\\p{Cntrl}&&[^\\r\\n\\t]]");
    private static final Duration AI_TIMEOUT = Duration.ofSeconds(60);
    private static final int MAX_PROMPT_LENGTH = 10000;

    private final ChatClient anthropicChatClient;
    private final ChatClient openAIChatClient;
    private final ChatClient ollamaChatClient;
    private final Resource systemPrompt;
    private final Executor aiExecutor;

    public AIService(
            @Qualifier("anthropicChatClient") ChatClient anthropicChatClient,
            @Qualifier("openAIChatClient") ChatClient openAIChatClient,
            @Qualifier("ollamaChatClient") ChatClient ollamaChatClient,
            @Value("classpath:static/system-prompt-v1.st") Resource systemPrompt,
            @Qualifier("aiExecutor") Executor aiExecutor,
            AIHistoryService historyService
    ) {
        this.anthropicChatClient = anthropicChatClient;
        this.openAIChatClient = openAIChatClient;
        this.ollamaChatClient = ollamaChatClient;
        this.systemPrompt = systemPrompt;
        this.aiHistoryService = historyService;
        this.aiExecutor = aiExecutor;
    }


    private Map<AIProvider, ChatClient> getProviders() {
    return Map.of(
            AIProvider.ANTHROPIC, anthropicChatClient,
            AIProvider.OPENAI, openAIChatClient,
            AIProvider.OLLAMA, ollamaChatClient
    );
}

    
public String generateSingleResponse(
        AIProvider provider,
        String prompt
) {
    validatePrompt(prompt);

    ChatClient client = switch (provider) {
        case ANTHROPIC -> anthropicChatClient;
        case OPENAI -> openAIChatClient;
        case OLLAMA -> ollamaChatClient;
    };

    return generateSingleResponse(client, prompt);
}
    
    public AIResponse generateResponses(String prompt) {
        validatePrompt(prompt);

        var anthropic = generateAsync(anthropicChatClient, prompt);
        var openAI = generateAsync(openAIChatClient, prompt);
        var ollama = generateAsync(ollamaChatClient, prompt);

        return AIResponse.builder()
                .anthropic(getResult(anthropic))
                .openAI(getResult(openAI))
                .ollama(getResult(ollama))
                .build();
    }

    private CompletableFuture<String> generateAsync(
            ChatClient client,
            String prompt
    ) {
        return CompletableFuture
                .supplyAsync(() -> generateSingleResponse(client, prompt), aiExecutor)
                .orTimeout(AI_TIMEOUT.toSeconds(), TimeUnit.SECONDS)
                .exceptionally(ex -> {
                    log.error("AI provider timeout or failure", ex);
                    return null;
                });

    }

    public String generateSingleResponse(ChatClient client, String prompt) {
        try {
            String response = client.prompt()
                    .system(systemPrompt)
                    .user(prompt)
                    .call()
                    .content();

            return validateResponse(response) ? response : null;

        } catch (Exception e) {
            log.error("AI provider failed", e);
            return null;
        }
    }

    private String getResult(CompletableFuture<String> future) {
        try {
            return future.join();
        } catch (CompletionException e) {
            log.error("Could not retrieve AI result", e);
            return null;
        }
    }

    public void validatePrompt(String prompt) {
        if (prompt == null || prompt.isBlank()) {
            throw new IllegalArgumentException("Prompt cannot be empty");
        }

        if (prompt.length() > MAX_PROMPT_LENGTH) {
            throw new IllegalArgumentException("Prompt exceeds maximum length");
        }
    }

    public boolean validateResponse(String response) {
        return response != null && !response.isBlank();
    }

    public int countSuccessfulResponses(AIResponse response) {
        Objects.requireNonNull(response, "AIResponse cannot be null");

        return (int) Stream.of(
                response.getAnthropic(),
                response.getOpenAI(),
                response.getOllama()
        ).filter(this::validateResponse).count();
    }

    public Map<String, String> getAvailableResponses(AIResponse response) {
        Objects.requireNonNull(response, "AIResponse cannot be null");

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
}
