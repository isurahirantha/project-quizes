package com.quizapp.module.quiz.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class QuizRequest {
    @NotNull(message = "Subcategory ID is required")
    private Long subcategoryId;

    @NotBlank(message = "Title is required")
    @Size(max = 500)
    private String title;

    private String description;

    private boolean isFree = false;

    @Min(value = 1) @Max(value = 100)
    private int passMark = 60;

    private boolean active = true;
}
