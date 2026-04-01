package com.quizapp.module.attempt.service;

import com.quizapp.common.enums.AttemptStatus;
import com.quizapp.exception.BadRequestException;
import com.quizapp.exception.ResourceNotFoundException;
import com.quizapp.module.accesscode.entity.AccessCode;
import com.quizapp.module.accesscode.repository.AccessCodeRepository;
import com.quizapp.module.attempt.dto.*;
import com.quizapp.module.attempt.entity.AttemptAnswer;
import com.quizapp.module.attempt.entity.QuizAttempt;
import com.quizapp.module.attempt.repository.AttemptAnswerRepository;
import com.quizapp.module.attempt.repository.QuizAttemptRepository;
import com.quizapp.module.question.entity.Question;
import com.quizapp.module.question.entity.QuestionOption;
import com.quizapp.module.question.repository.QuestionRepository;
import com.quizapp.module.quiz.entity.Quiz;
import com.quizapp.module.quiz.service.QuizService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class QuizAttemptService {

    private final QuizAttemptRepository attemptRepository;
    private final AttemptAnswerRepository answerRepository;
    private final QuizService quizService;
    private final QuestionRepository questionRepository;
    private final AccessCodeRepository accessCodeRepository;

    @Transactional
    public StartAttemptResponse startAttempt(StartAttemptRequest request) {
        Quiz quiz = quizService.findActive(request.getQuizId());

        // Access control
        if (!quiz.isFree()) {
            validateAccessCode(request, quiz);
        }

        List<Question> questions = questionRepository.findByQuizIdWithOptions(quiz.getId());
        if (questions.isEmpty()) {
            throw new BadRequestException("This quiz has no questions yet");
        }

        QuizAttempt attempt = QuizAttempt.builder()
                .quiz(quiz)
                .userEmail(request.getEmail())
                .userMobile(request.getMobile())
                .deviceId(request.getDeviceId())
                .accessCodeUsed(request.getAccessCode())
                .status(AttemptStatus.IN_PROGRESS)
                .currentIndex(0)
                .totalQuestions(questions.size())
                .build();

        attempt = attemptRepository.save(attempt);

        StartAttemptResponse res = new StartAttemptResponse();
        res.setAttemptId(attempt.getId());
        res.setQuizId(quiz.getId());
        res.setQuizTitle(quiz.getTitle());
        res.setTotalQuestions(questions.size());
        res.setCurrentIndex(0);
        res.setStatus(attempt.getStatus().name());
        return res;
    }

    public QuestionAttemptView getQuestion(Long attemptId, int index) {
        QuizAttempt attempt = getInProgressAttempt(attemptId);
        List<Question> questions = questionRepository.findByQuizIdWithOptions(attempt.getQuiz().getId());

        if (index < 0 || index >= questions.size()) {
            throw new BadRequestException("Invalid question index: " + index);
        }

        // Update current index
        attempt.setCurrentIndex(index);
        attemptRepository.save(attempt);

        Question q = questions.get(index);
        Optional<AttemptAnswer> existingAnswer = answerRepository
                .findByAttemptIdAndQuestionId(attemptId, q.getId());

        QuestionAttemptView view = new QuestionAttemptView();
        view.setQuestionId(q.getId());
        view.setQuestionText(q.getQuestionText());
        view.setQuestionType(q.getQuestionType());
        view.setOrderIndex(q.getOrderIndex());
        view.setCurrentIndex(index);
        view.setTotalQuestions(questions.size());
        view.setHasNext(index < questions.size() - 1);
        view.setHasPrevious(index > 0);
        view.setOptions(q.getOptions().stream().map(opt -> {
            OptionAttemptView ov = new OptionAttemptView();
            ov.setId(opt.getId());
            ov.setOptionText(opt.getOptionText());
            ov.setOrderIndex(opt.getOrderIndex());
            return ov;
        }).toList());
        view.setSelectedOptionIds(existingAnswer.map(AttemptAnswer::getSelectedOptionIds).orElse(List.of()));
        return view;
    }

    @Transactional
    public void submitAnswer(Long attemptId, SubmitAnswerRequest request) {
        QuizAttempt attempt = getInProgressAttempt(attemptId);

        Question question = questionRepository.findById(request.getQuestionId())
                .orElseThrow(() -> new ResourceNotFoundException("Question", request.getQuestionId()));

        // Determine correctness
        Set<Long> correctIds = new HashSet<>();
        for (QuestionOption opt : question.getOptions()) {
            if (opt.isCorrect()) correctIds.add(opt.getId());
        }
        Set<Long> selected = new HashSet<>(request.getSelectedOptionIds());
        boolean isCorrect = correctIds.equals(selected);

        // Upsert answer (replace if navigated back)
        AttemptAnswer answer = answerRepository
                .findByAttemptIdAndQuestionId(attemptId, question.getId())
                .orElse(AttemptAnswer.builder().attempt(attempt).question(question).build());

        answer.setSelectedOptionIds(request.getSelectedOptionIds());
        answer.setCorrect(isCorrect);
        answer.setAnsweredAt(LocalDateTime.now());
        answerRepository.save(answer);
    }

    @Transactional
    public AttemptResultResponse submitQuiz(Long attemptId) {
        QuizAttempt attempt = getInProgressAttempt(attemptId);

        List<Question> questions = questionRepository.findByQuizIdWithOptions(attempt.getQuiz().getId());
        List<AttemptAnswer> answers = answerRepository.findByAttemptId(attemptId);

        int correct = (int) answers.stream().filter(AttemptAnswer::isCorrect).count();
        int incorrect = (int) answers.stream().filter(a -> !a.isCorrect()).count();
        int total = questions.size();
        int skipped = total - answers.size();

        BigDecimal scorePercent = total > 0
                ? BigDecimal.valueOf(correct * 100.0 / total).setScale(2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;
        boolean passed = scorePercent.intValue() >= attempt.getQuiz().getPassMark();

        attempt.setStatus(AttemptStatus.COMPLETED);
        attempt.setTotalQuestions(total);
        attempt.setCorrectCount(correct);
        attempt.setIncorrectCount(incorrect);
        attempt.setScorePercent(scorePercent);
        attempt.setPassed(passed);
        attempt.setCompletedAt(LocalDateTime.now());
        attemptRepository.save(attempt);

        return buildResult(attempt, skipped);
    }

    public AttemptResultResponse getResult(Long attemptId) {
        QuizAttempt attempt = attemptRepository.findByIdAndStatus(attemptId, AttemptStatus.COMPLETED)
                .orElseThrow(() -> new BadRequestException("Attempt not completed or not found"));
        List<AttemptAnswer> answers = answerRepository.findByAttemptId(attemptId);
        int skipped = attempt.getTotalQuestions() - answers.size();
        return buildResult(attempt, skipped);
    }

    private AttemptResultResponse buildResult(QuizAttempt attempt, int skipped) {
        AttemptResultResponse r = new AttemptResultResponse();
        r.setAttemptId(attempt.getId());
        r.setQuizId(attempt.getQuiz().getId());
        r.setQuizTitle(attempt.getQuiz().getTitle());
        r.setTotalQuestions(attempt.getTotalQuestions());
        r.setCorrectCount(attempt.getCorrectCount());
        r.setIncorrectCount(attempt.getIncorrectCount());
        r.setSkippedCount(skipped);
        r.setScorePercent(attempt.getScorePercent());
        r.setPassMark(attempt.getQuiz().getPassMark());
        r.setPassed(Boolean.TRUE.equals(attempt.getPassed()));
        r.setCompletedAt(attempt.getCompletedAt() != null ? attempt.getCompletedAt().toString() : null);
        return r;
    }

    private QuizAttempt getInProgressAttempt(Long attemptId) {
        return attemptRepository.findByIdAndStatus(attemptId, AttemptStatus.IN_PROGRESS)
                .orElseThrow(() -> new BadRequestException("Attempt not found or already completed"));
    }

    private void validateAccessCode(StartAttemptRequest request, Quiz quiz) {
        if (request.getAccessCode() == null || request.getAccessCode().isBlank()) {
            throw new BadRequestException("This is a paid quiz. Please provide a valid access code.");
        }

        AccessCode accessCode = accessCodeRepository.findByCodeAndActiveTrue(request.getAccessCode())
                .orElseThrow(() -> new BadRequestException("Invalid access code"));

        if (accessCode.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new BadRequestException("Access code has expired");
        }

        // Check category matches
        Long quizCategoryId = quiz.getSubcategory().getCategory().getId();
        if (!accessCode.getCategory().getId().equals(quizCategoryId)) {
            throw new BadRequestException("Access code is not valid for this quiz category");
        }

        // Device binding: bind on first use or verify subsequent use
        if (accessCode.getBoundDeviceId() == null && request.getDeviceId() != null) {
            accessCode.setBoundDeviceId(request.getDeviceId());
            accessCodeRepository.save(accessCode);
        } else if (accessCode.getBoundDeviceId() != null && request.getDeviceId() != null
                && !accessCode.getBoundDeviceId().equals(request.getDeviceId())) {
            throw new BadRequestException("Access code is bound to a different device");
        }
    }
}
