package com.quizapp.module.attempt.dto;

import com.quizapp.common.enums.QuestionType;
import lombok.Data;

import java.util.List;

@Data
public class QuestionAttemptView {
    private Long questionId;
    private String questionText;
    private QuestionType questionType;
    private int orderIndex;
    private int currentIndex;      // 0-based position in list
    private int totalQuestions;
    private boolean hasNext;
    private boolean hasPrevious;
    private List<OptionAttemptView> options;
    private List<Long> selectedOptionIds;  // already answered (for navigation back)
}
