public record AIComparisonRequestDto(
        @NotNull
        Long conversationId,

        @NotBlank
        @Size(max = 10000)
        String prompt
) {}
