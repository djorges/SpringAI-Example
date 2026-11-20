@Service
@RequiredArgsConstructor
public class AIApplicationService {

    private final AIService aiService;
    private final AIHistoryService aiHistoryService;

    @Transactional
    public AIResponse generateAndSave(
            Long conversationId,
            String prompt
    ) {
        AIResponse response = aiService.generateResponses(prompt);

        aiHistoryService.saveExecution(
                conversationId,
                prompt,
                response
        );

        return response;
    }
}
