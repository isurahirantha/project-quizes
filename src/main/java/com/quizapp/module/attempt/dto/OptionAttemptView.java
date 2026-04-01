package com.quizapp.module.attempt.dto;

import lombok.Data;

@Data
public class OptionAttemptView {
    private Long id;
    private String optionText;
    private int orderIndex;
    // NOTE: isCorrect intentionally excluded — leaking answers would defeat the quiz
}
