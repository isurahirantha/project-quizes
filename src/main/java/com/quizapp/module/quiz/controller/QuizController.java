package com.quizapp.module.quiz.controller;

import com.quizapp.common.ApiResponse;
import com.quizapp.common.PageResponse;
import com.quizapp.module.quiz.dto.QuizRequest;
import com.quizapp.module.quiz.dto.QuizResponse;
import com.quizapp.module.quiz.service.QuizService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@Tag(name = "Quiz", description = "Quiz management and search")
public class QuizController {

    private final QuizService quizService;

    @PostMapping("/api/admin/quizzes")
    @Operation(summary = "Create quiz [Admin]", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<QuizResponse>> create(@Valid @RequestBody QuizRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Quiz created", quizService.create(request)));
    }

    @GetMapping("/api/quizzes")
    @Operation(summary = "Search/filter quizzes [Public]")
    public ResponseEntity<ApiResponse<PageResponse<QuizResponse>>> listPublic(
            @RequestParam(required = false) Long subcategoryId,
            @RequestParam(required = false) Boolean free,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ApiResponse.ok(quizService.listPublic(subcategoryId, free, search, page, size)));
    }

    @GetMapping("/api/quizzes/{id}")
    @Operation(summary = "Get quiz by ID [Public]")
    public ResponseEntity<ApiResponse<QuizResponse>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(quizService.getById(id)));
    }

    @PutMapping("/api/admin/quizzes/{id}")
    @Operation(summary = "Update quiz [Admin]", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<QuizResponse>> update(
            @PathVariable Long id, @Valid @RequestBody QuizRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Quiz updated", quizService.update(id, request)));
    }

    @DeleteMapping("/api/admin/quizzes/{id}")
    @Operation(summary = "Delete quiz (soft) [Admin]", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        quizService.delete(id);
        return ResponseEntity.ok(ApiResponse.ok("Deleted", null));
    }
}
