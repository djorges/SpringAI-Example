package com.example.ia.spring.controller;

import com.example.ia.spring.model.AIResponse;
import com.example.ia.spring.service.AIService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/chats")
@RequiredArgsConstructor
public class ChatController {
    private final AIService aiService;

    @GetMapping("/compare")
    public AIResponse compare(@RequestParam String prompt) {
        return aiService.generateResponses(prompt);
    }
}
