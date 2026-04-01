package com.quizapp.module.attempt.dto;

import lombok.Data;

@Data
public class StartAttemptResponse {
    private Long attemptId;
    private Long quizId;
    private String quizTitle;
    private int totalQuestions;
    private int currentIndex;
    private String status;
}
