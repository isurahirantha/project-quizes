package com.quizapp.module.attempt.controller;

import com.quizapp.common.ApiResponse;
import com.quizapp.module.attempt.dto.*;
import com.quizapp.module.attempt.service.QuizAttemptService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/attempts")
@RequiredArgsConstructor
@Tag(name = "Quiz Attempt", description = "Public quiz attempt flow – start, navigate, submit")
public class QuizAttemptController {

    private final QuizAttemptService attemptService;

    @PostMapping("/start")
    @Operation(summary = "Start a quiz attempt [Public]")
    public ResponseEntity<ApiResponse<StartAttemptResponse>> start(
            @Valid @RequestBody StartAttemptRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Quiz started", attemptService.startAttempt(request)));
    }

    @GetMapping("/{attemptId}/question")
    @Operation(summary = "Get question at index (with navigation state) [Public]")
    public ResponseEntity<ApiResponse<QuestionAttemptView>> getQuestion(
            @PathVariable Long attemptId,
            @RequestParam(defaultValue = "0") int index) {
        return ResponseEntity.ok(ApiResponse.ok(attemptService.getQuestion(attemptId, index)));
    }

    @GetMapping("/{attemptId}/question/next")
    @Operation(summary = "Get next question [Public]")
    public ResponseEntity<ApiResponse<QuestionAttemptView>> nextQuestion(@PathVariable Long attemptId) {
        QuestionAttemptView current = attemptService.getQuestion(attemptId, 0);
        int nextIndex = current.getCurrentIndex() + 1;
        return ResponseEntity.ok(ApiResponse.ok(attemptService.getQuestion(attemptId, nextIndex)));
    }

    @GetMapping("/{attemptId}/question/previous")
    @Operation(summary = "Get previous question [Public]")
    public ResponseEntity<ApiResponse<QuestionAttemptView>> previousQuestion(@PathVariable Long attemptId) {
        QuestionAttemptView current = attemptService.getQuestion(attemptId, 0);
        int prevIndex = Math.max(0, current.getCurrentIndex() - 1);
        return ResponseEntity.ok(ApiResponse.ok(attemptService.getQuestion(attemptId, prevIndex)));
    }

    @PostMapping("/{attemptId}/answer")
    @Operation(summary = "Submit answer for a question [Public]")
    public ResponseEntity<ApiResponse<Void>> submitAnswer(
            @PathVariable Long attemptId,
            @Valid @RequestBody SubmitAnswerRequest request) {
        attemptService.submitAnswer(attemptId, request);
        return ResponseEntity.ok(ApiResponse.ok("Answer saved", null));
    }

    @PostMapping("/{attemptId}/submit")
    @Operation(summary = "Submit the quiz and get results [Public]")
    public ResponseEntity<ApiResponse<AttemptResultResponse>> submitQuiz(@PathVariable Long attemptId) {
        return ResponseEntity.ok(ApiResponse.ok("Quiz completed", attemptService.submitQuiz(attemptId)));
    }

    @GetMapping("/{attemptId}/result")
    @Operation(summary = "Get quiz result for a completed attempt [Public]")
    public ResponseEntity<ApiResponse<AttemptResultResponse>> getResult(@PathVariable Long attemptId) {
        return ResponseEntity.ok(ApiResponse.ok(attemptService.getResult(attemptId)));
    }
}
