package com.lifequest.dto;
import jakarta.validation.constraints.*;
public record CreateQuestRequest(@NotBlank @Size(max=200) String title,@NotNull @Min(1) @Max(1000) Integer xp,@NotBlank String difficulty,@Size(max=80) String clientRequestId) {}
