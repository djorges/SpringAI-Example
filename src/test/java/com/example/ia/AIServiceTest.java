
@ExtendWith(MockitoExtension.class)
class AIServiceTest {

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ChatClient anthropicChatClient;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ChatClient openAIChatClient;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ChatClient ollamaChatClient;

    @Mock
    private Resource systemPrompt;

    @Mock
    private AIHistoryService aiHistoryService;

    private AIService aiService;

    @BeforeEach
    void setUp() {
        aiService = new AIService(
                anthropicChatClient,
                openAIChatClient,
                ollamaChatClient,
                systemPrompt,
                Runnable::run,
                aiHistoryService
        );
    }

    private void mockResponse(ChatClient client, String response) {
        when(client.prompt()
                .system(any(Resource.class))
                .user(anyString())
                .call()
                .content())
                .thenReturn(response);
    }

    @Test
    void generateSingleResponse_shouldReturnResponse() {
        mockResponse(anthropicChatClient, "AI response");

        String result = aiService.generateSingleResponse(
                anthropicChatClient,
                "Hello"
        );

        assertThat(result).isEqualTo("AI response");
    }

    @Test
    void generateSingleResponse_shouldReturnNullWhenEmpty() {
        mockResponse(anthropicChatClient, " ");

        String result = aiService.generateSingleResponse(
                anthropicChatClient,
                "Hello"
        );

        assertThat(result).isNull();
    }

    @Test
    void generateSingleResponse_shouldHandleException() {
        when(anthropicChatClient.prompt()).thenThrow(
                new RuntimeException("Provider error")
        );

        String result = aiService.generateSingleResponse(
                anthropicChatClient,
                "Hello"
        );

        assertThat(result).isNull();
    }

    @Test
    void validatePrompt_shouldAcceptValidPrompt() {
        assertThatCode(() -> aiService.validatePrompt("Hello"))
                .doesNotThrowAnyException();
    }

    @Test
    void validatePrompt_shouldRejectNull() {
        assertThatThrownBy(() -> aiService.validatePrompt(null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void validatePrompt_shouldRejectBlank() {
        assertThatThrownBy(() -> aiService.validatePrompt("   "))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void validatePrompt_shouldRejectLongPrompt() {
        String prompt = "a".repeat(10001);

        assertThatThrownBy(() -> aiService.validatePrompt(prompt))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void validatePrompt_shouldAcceptMaximumLength() {
        String prompt = "a".repeat(10000);

        assertThatCode(() -> aiService.validatePrompt(prompt))
                .doesNotThrowAnyException();
    }

    @Test
    void validateResponse_shouldReturnTrue() {
        assertThat(aiService.validateResponse("Response"))
                .isTrue();
    }

    @Test
    void validateResponse_shouldReturnFalseForNull() {
        assertThat(aiService.validateResponse(null))
                .isFalse();
    }

    @Test
    void validateResponse_shouldReturnFalseForBlank() {
        assertThat(aiService.validateResponse(" "))
                .isFalse();
    }

    @Test
    void countSuccessfulResponses_shouldCountValidResponses() {
        AIResponse response = AIResponse.builder()
                .anthropic("Response 1")
                .openAI("Response 2")
                .ollama(null)
                .build();

        int result = aiService.countSuccessfulResponses(response);

        assertThat(result).isEqualTo(2);
    }

    @Test
    void countSuccessfulResponses_shouldReturnZero() {
        AIResponse response = AIResponse.builder()
                .anthropic(null)
                .openAI("")
                .ollama(null)
                .build();

        assertThat(aiService.countSuccessfulResponses(response))
                .isZero();
    }

    @Test
    void countSuccessfulResponses_shouldRejectNull() {
        assertThatThrownBy(() -> aiService.countSuccessfulResponses(null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void getAvailableResponses_shouldReturnOnlyValidResponses() {
        AIResponse response = AIResponse.builder()
                .anthropic("Anthropic response")
                .openAI(null)
                .ollama("Ollama response")
                .build();

        Map<String, String> result =
                aiService.getAvailableResponses(response);

        assertThat(result)
                .containsOnlyKeys("Anthropic", "Ollama")
                .hasSize(2);
    }

    @Test
    void getAvailableResponses_shouldReturnEmptyMap() {
        AIResponse response = AIResponse.builder()
                .anthropic(null)
                .openAI("")
                .ollama(null)
                .build();

        assertThat(aiService.getAvailableResponses(response))
                .isEmpty();
    }

    @Test
    void generateResponses_shouldReturnAllProviderResponses() {
        mockResponse(anthropicChatClient, "Anthropic");
        mockResponse(openAIChatClient, "OpenAI");
        mockResponse(ollamaChatClient, "Ollama");

        AIResponse result = aiService.generateResponses("Hello");

        assertThat(result.getAnthropic()).isEqualTo("Anthropic");
        assertThat(result.getOpenAI()).isEqualTo("OpenAI");
        assertThat(result.getOllama()).isEqualTo("Ollama");
    }

    @Test
    void generateResponses_shouldRejectInvalidPrompt() {
        assertThatThrownBy(() -> aiService.generateResponses(""))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void generateResponses_shouldContinueWhenProviderFails() {
        mockResponse(anthropicChatClient, "Anthropic");

        when(openAIChatClient.prompt()).thenThrow(
                new RuntimeException("OpenAI error")
        );

        mockResponse(ollamaChatClient, "Ollama");

        AIResponse result = aiService.generateResponses("Hello");

        assertThat(result.getAnthropic()).isEqualTo("Anthropic");
        assertThat(result.getOpenAI()).isNull();
        assertThat(result.getOllama()).isEqualTo("Ollama");
    }

    @Test
    void generateAndSave_shouldSaveExecution() {
        mockResponse(anthropicChatClient, "Anthropic");
        mockResponse(openAIChatClient, "OpenAI");
        mockResponse(ollamaChatClient, "Ollama");

        AIResponse result = aiService.generateAndSave(1L, "Hello");

        assertThat(result).isNotNull();

        verify(aiHistoryService).saveExecution(
                eq(1L),
                eq("Hello"),
                any(AIResponse.class)
        );
    }

    @Test
    void generateAndSave_shouldNotSaveWhenPromptIsInvalid() {
        assertThatThrownBy(() -> aiService.generateAndSave(1L, " "))
                .isInstanceOf(IllegalArgumentException.class);

        verifyNoInteractions(aiHistoryService);
    }
}
