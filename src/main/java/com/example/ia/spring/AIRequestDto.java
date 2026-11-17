public record AIRequestDto(
        @NotBlank
        @Size(max = 10000)
        String prompt
) {}
