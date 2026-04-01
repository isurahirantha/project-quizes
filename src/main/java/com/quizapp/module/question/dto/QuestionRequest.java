package com.quizapp.module.question.dto;

import com.quizapp.common.enums.QuestionType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.util.List;

@Data
public class QuestionRequest {
    @NotNull
    private Long quizId;

    @NotBlank(message = "Question text is required")
    private String questionText;

    @NotNull(message = "Question type is required (SINGLE or MULTIPLE)")
    private QuestionType questionType;

    private int orderIndex = 0;

    @NotEmpty(message = "At least 2 options are required")
    @Size(min = 2, max = 6)
    @Valid
    private List<OptionRequest> options;
}
