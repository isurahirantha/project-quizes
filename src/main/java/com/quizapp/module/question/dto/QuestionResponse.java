package com.quizapp.module.question.dto;

import com.quizapp.common.enums.QuestionType;
import lombok.Data;
import java.util.List;

@Data
public class QuestionResponse {
    private Long id;
    private Long quizId;
    private String questionText;
    private QuestionType questionType;
    private int orderIndex;
    private List<OptionResponse> options;
}
