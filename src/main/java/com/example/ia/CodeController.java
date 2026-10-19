package com.example.ia;

import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/openai")
public class CodeController {
    private final ChatClient chatClient;

    private final Resource systemPrompt;

    public CodeController(ChatClient.Builder builder,
                          @Value("classpath:static/system-prompt-v1.st")
                          Resource systemPrompt) {
        this.chatClient = builder.build();
        this.systemPrompt = systemPrompt;
    }

    @GetMapping("/generate-code")
    public String generateCode(@RequestParam String prompt) {
        return chatClient.prompt(prompt)
                .system(systemPrompt)
                .call().content();
    }
}
