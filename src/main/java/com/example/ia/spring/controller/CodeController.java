package com.example.ia.spring.controller;

import com.example.ia.spring.model.MovieCuriosities;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;


@RestController
@RequestMapping("/api/openai")
public class CodeController {

    private final ChatClient chatClient;
    private final Resource systemPrompt;

    public CodeController(
            @Qualifier("openAIChatClient") ChatClient chatClient,
            @Value("classpath:static/system-prompt-v1.st")
            Resource systemPrompt
    ) {
        this.chatClient = chatClient;
        this.systemPrompt = systemPrompt;
    }

    @GetMapping("/generate-code")
    public String generateCode(@RequestParam String prompt) {
        return chatClient.prompt()
                .system(systemPrompt)
                .user(prompt)
                .call()
                .content();
    }

    @GetMapping("/generate-code-reactive")
    public Flux<String> generateCodeReactive(
            @RequestParam String prompt
    ) {
        return chatClient.prompt()
                .system(systemPrompt)
                .user(prompt)
                .stream()
                .content();
    }

    @GetMapping("/movies/generate-code-entity")
    public MovieCuriosities generateCodeMovies() {
        return chatClient.prompt()
                .system(systemPrompt)
                .user("Dame 4 datos curiosos de Star Wars")
                .call()
                .entity(MovieCuriosities.class);
    }
}

