package com.quizapp.module.question.service;

import com.quizapp.exception.BadRequestException;
import com.quizapp.exception.ResourceNotFoundException;
import com.quizapp.module.question.dto.*;
import com.quizapp.module.question.entity.Question;
import com.quizapp.module.question.entity.QuestionOption;
import com.quizapp.module.question.repository.QuestionRepository;
import com.quizapp.module.quiz.entity.Quiz;
import com.quizapp.module.quiz.service.QuizService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class QuestionService {

    private final QuestionRepository questionRepository;
    private final QuizService quizService;

    @Transactional
    public QuestionResponse create(QuestionRequest request) {
        Quiz quiz = quizService.findActive(request.getQuizId());

        validateOptions(request);

        Question question = Question.builder()
                .quiz(quiz)
                .questionText(request.getQuestionText())
                .questionType(request.getQuestionType())
                .orderIndex(request.getOrderIndex())
                .deleted(false)
                .build();

        // Map options from request — admin provides isCorrect flag
        for (OptionRequest opt : request.getOptions()) {
            QuestionOption option = QuestionOption.builder()
                    .question(question)
                    .optionText(opt.getOptionText())
                    .isCorrect(opt.isCorrect())
                    .orderIndex(opt.getOrderIndex())
                    .build();
            question.getOptions().add(option);
        }

        return toResponse(questionRepository.save(question));
    }

    public List<QuestionResponse> listByQuiz(Long quizId) {
        return questionRepository.findByQuizIdWithOptions(quizId)
                .stream().map(this::toResponse).toList();
    }

    @Transactional
    public QuestionResponse update(Long id, QuestionRequest request) {
        Question question = findActive(id);
        validateOptions(request);

        question.setQuestionText(request.getQuestionText());
        question.setQuestionType(request.getQuestionType());
        question.setOrderIndex(request.getOrderIndex());

        // Replace options
        question.getOptions().clear();
        for (OptionRequest opt : request.getOptions()) {
            QuestionOption option = QuestionOption.builder()
                    .question(question)
                    .optionText(opt.getOptionText())
                    .isCorrect(opt.isCorrect())
                    .orderIndex(opt.getOrderIndex())
                    .build();
            question.getOptions().add(option);
        }

        return toResponse(questionRepository.save(question));
    }

    @Transactional
    public void delete(Long id) {
        Question q = findActive(id);
        q.setDeleted(true);
        questionRepository.save(q);
    }

    private void validateOptions(QuestionRequest request) {
        long correctCount = request.getOptions().stream().filter(OptionRequest::isCorrect).count();
        if (correctCount == 0) {
            throw new BadRequestException("At least one correct answer must be marked");
        }
        if (request.getQuestionType().name().equals("SINGLE") && correctCount > 1) {
            throw new BadRequestException("SINGLE type question can only have one correct answer");
        }
    }

    public Question findActive(Long id) {
        Question q = questionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Question", id));
        if (q.isDeleted()) throw new ResourceNotFoundException("Question", id);
        return q;
    }

    public QuestionResponse toResponse(Question q) {
        QuestionResponse r = new QuestionResponse();
        r.setId(q.getId());
        r.setQuizId(q.getQuiz().getId());
        r.setQuestionText(q.getQuestionText());
        r.setQuestionType(q.getQuestionType());
        r.setOrderIndex(q.getOrderIndex());
        r.setOptions(q.getOptions().stream().map(opt -> {
            OptionResponse or = new OptionResponse();
            or.setId(opt.getId());
            or.setOptionText(opt.getOptionText());
            or.setCorrect(opt.isCorrect());
            or.setOrderIndex(opt.getOrderIndex());
            return or;
        }).toList());
        return r;
    }
}
