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
@Validated
public class ChatController {

    private final AIService aiService;
    private final AIApplicationService aiApplicationService;

    @PostMapping("/compare")
    public ResponseEntity<AIResponse> compare(
            @Valid @RequestBody AIRequestDto request
    ) {
        return ResponseEntity.ok(
                aiService.generateResponses(request.prompt())
        );
    }

    @PostMapping("/compare/save")
    public ResponseEntity<AIResponse> compareAndSave(
            @Valid @RequestBody AIComparisonRequestDto request
    ) {
        return ResponseEntity.ok(
                aiApplicationService.generateAndSave(
                        request.conversationId(),
                        request.prompt()
                )
        );
    }

    @PostMapping("/anthropic")
    public ResponseEntity<String> anthropic(
            @Valid @RequestBody AIRequestDto request
    ) {
        return ResponseEntity.ok(
                aiService.generateSingleResponse(
                        AIProvider.ANTHROPIC,
                        request.prompt()
                )
        );
    }

    @PostMapping("/openai")
    public ResponseEntity<String> openAI(
            @Valid @RequestBody AIRequestDto request
    ) {
        return ResponseEntity.ok(
                aiService.generateSingleResponse(
                        AIProvider.OPENAI,
                        request.prompt()
                )
        );
    }

    @PostMapping("/ollama")
    public ResponseEntity<String> ollama(
            @Valid @RequestBody AIRequestDto request
    ) {
        return ResponseEntity.ok(
                aiService.generateSingleResponse(
                        AIProvider.OLLAMA,
                        request.prompt()
                )
        );
    }
}
