package com.quizapp.module.question.controller;

import com.quizapp.common.ApiResponse;
import com.quizapp.module.question.dto.QuestionRequest;
import com.quizapp.module.question.dto.QuestionResponse;
import com.quizapp.module.question.service.QuestionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Tag(name = "Question", description = "Quiz questions management (Admin provides questions and answers)")
public class QuestionController {

    private final QuestionService questionService;

    @PostMapping("/api/admin/questions")
    @Operation(summary = "Create question with options and correct answers [Admin]",
               security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<QuestionResponse>> create(@Valid @RequestBody QuestionRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Question created", questionService.create(request)));
    }

    @GetMapping("/api/admin/questions")
    @Operation(summary = "List questions for a quiz (with correct answers) [Admin]",
               security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<List<QuestionResponse>>> listByQuiz(@RequestParam Long quizId) {
        return ResponseEntity.ok(ApiResponse.ok(questionService.listByQuiz(quizId)));
    }

    @PutMapping("/api/admin/questions/{id}")
    @Operation(summary = "Update question and its options [Admin]",
               security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<QuestionResponse>> update(
            @PathVariable Long id, @Valid @RequestBody QuestionRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Question updated", questionService.update(id, request)));
    }

    @DeleteMapping("/api/admin/questions/{id}")
    @Operation(summary = "Delete question (soft) [Admin]",
               security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        questionService.delete(id);
        return ResponseEntity.ok(ApiResponse.ok("Question deleted", null));
    }
}
