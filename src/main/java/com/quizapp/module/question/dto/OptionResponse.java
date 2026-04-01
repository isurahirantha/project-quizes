package com.quizapp.module.question.dto;

import lombok.Data;

@Data
public class OptionResponse {
    private Long id;
    private String optionText;
    private boolean isCorrect;   // shown to admin only; hide for public attempt API
    private int orderIndex;
}
