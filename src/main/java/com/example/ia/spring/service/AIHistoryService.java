
@Slf4j
@Service
@RequiredArgsConstructor
public class AIHistoryService {

    private final ConversationRepository conversationRepository;
    private final MessageRepository messageRepository;
    private final AIResponseRepository responseRepository;

    @Transactional
    public Long saveExecution(
            Long conversationId,
            String prompt,
            AIResponse response) {

        ConversationEntity conversation = conversationRepository
                .findById(conversationId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Conversation not found"
                ));

        MessageEntity message = MessageEntity.builder()
                .conversation(conversation)
                .role(MessageRole.USER)
                .content(prompt)
                .build();

        messageRepository.save(message);

        List<AIResponseEntity> responses = List.of(
                createResponse(message, AIProvider.ANTHROPIC,
                        response.getAnthropic()),
                createResponse(message, AIProvider.OPENAI,
                        response.getOpenAI()),
                createResponse(message, AIProvider.OLLAMA,
                        response.getOllama())
        );

        responseRepository.saveAll(responses);

        return message.getId();
    }

    private AIResponseEntity createResponse(
            MessageEntity message,
            AIProvider provider,
            String content) {

        boolean successful = content != null && !content.isBlank();

        return AIResponseEntity.builder()
                .message(message)
                .provider(provider)
                .content(content)
                .status(successful
                        ? ResponseStatus.SUCCESS
                        : ResponseStatus.FAILED)
                .errorMessage(successful ? null : "Provider failed")
                .build();
    }

    @Transactional(readOnly = true)
    public List<MessageEntity> getConversationHistory(Long conversationId) {

        return messageRepository
                .findByConversationIdOrderByCreatedAtAsc(conversationId);
    }

    @Transactional(readOnly = true)
    public List<AIResponseEntity> getMessageResponses(Long messageId) {

        return responseRepository.findByMessageId(messageId);
    }
}
