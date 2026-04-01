package com.quizapp.module.accesscode.dto;

import com.quizapp.module.quiz.dto.QuizResponse;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class AccessCodeValidateResponse {
    private String code;
    private String categoryName;
    private String userName;
    private LocalDateTime expiresAt;
    private boolean valid;
    private String message;
    private List<QuizResponse> accessibleQuizzes;
}
