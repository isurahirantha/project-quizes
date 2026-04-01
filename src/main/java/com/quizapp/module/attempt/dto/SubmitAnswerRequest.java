package com.quizapp.module.attempt.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.util.List;

@Data
public class SubmitAnswerRequest {
    @NotNull
    private Long questionId;

    @NotEmpty(message = "Select at least one option")
    private List<Long> selectedOptionIds;
}
