package com.quizapp.module.attempt.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class StartAttemptRequest {
    @NotNull(message = "Quiz ID is required")
    private Long quizId;

    private String accessCode;
    private String deviceId;
    private String email;
    private String mobile;
}
