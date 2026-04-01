package com.quizapp.module.attempt.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class AttemptResultResponse {
    private Long attemptId;
    private Long quizId;
    private String quizTitle;
    private int totalQuestions;
    private int correctCount;
    private int incorrectCount;
    private int skippedCount;
    private BigDecimal scorePercent;
    private int passMark;
    private boolean passed;
    private String completedAt;
}
