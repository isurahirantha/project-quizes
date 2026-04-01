package com.quizapp.module.quiz.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class QuizResponse {
    private Long id;
    private Long subcategoryId;
    private String subcategoryName;
    private Long categoryId;
    private String categoryName;
    private String title;
    private String description;
    private boolean free;
    private int passMark;
    private boolean active;
    private long questionCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
