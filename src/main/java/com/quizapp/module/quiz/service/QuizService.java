package com.quizapp.module.quiz.service;

import com.quizapp.common.PageResponse;
import com.quizapp.exception.ResourceNotFoundException;
import com.quizapp.module.question.repository.QuestionRepository;
import com.quizapp.module.quiz.dto.QuizRequest;
import com.quizapp.module.quiz.dto.QuizResponse;
import com.quizapp.module.quiz.entity.Quiz;
import com.quizapp.module.quiz.repository.QuizRepository;
import com.quizapp.module.subcategory.entity.Subcategory;
import com.quizapp.module.subcategory.repository.SubcategoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class QuizService {

    private final QuizRepository quizRepository;
    private final SubcategoryRepository subcategoryRepository;
    private final QuestionRepository questionRepository;

    @Transactional
    public QuizResponse create(QuizRequest request) {
        Subcategory sub = subcategoryRepository.findById(request.getSubcategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Subcategory", request.getSubcategoryId()));

        Quiz quiz = Quiz.builder()
                .subcategory(sub)
                .title(request.getTitle())
                .description(request.getDescription())
                .isFree(request.isFree())
                .passMark(request.getPassMark())
                .active(request.isActive())
                .deleted(false)
                .build();

        return toResponse(quizRepository.save(quiz));
    }

    public PageResponse<QuizResponse> listPublic(Long subcategoryId, Boolean isFree, String search,
                                                   int page, int size) {
        var pageable = PageRequest.of(page, size, Sort.by("title").ascending());
        return PageResponse.of(quizRepository.findPublic(subcategoryId, isFree, search, pageable)
                .map(this::toResponse));
    }

    public QuizResponse getById(Long id) {
        return toResponse(findActive(id));
    }

    @Transactional
    public QuizResponse update(Long id, QuizRequest request) {
        Quiz quiz = findActive(id);
        Subcategory sub = subcategoryRepository.findById(request.getSubcategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Subcategory", request.getSubcategoryId()));
        quiz.setSubcategory(sub);
        quiz.setTitle(request.getTitle());
        quiz.setDescription(request.getDescription());
        quiz.setFree(request.isFree());
        quiz.setPassMark(request.getPassMark());
        quiz.setActive(request.isActive());
        return toResponse(quizRepository.save(quiz));
    }

    @Transactional
    public void delete(Long id) {
        Quiz quiz = findActive(id);
        quiz.setDeleted(true);
        quizRepository.save(quiz);
    }

    public Quiz findActive(Long id) {
        Quiz quiz = quizRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Quiz", id));
        if (quiz.isDeleted()) throw new ResourceNotFoundException("Quiz", id);
        return quiz;
    }

    public QuizResponse toResponse(Quiz q) {
        QuizResponse r = new QuizResponse();
        r.setId(q.getId());
        r.setSubcategoryId(q.getSubcategory().getId());
        r.setSubcategoryName(q.getSubcategory().getName());
        r.setCategoryId(q.getSubcategory().getCategory().getId());
        r.setCategoryName(q.getSubcategory().getCategory().getName());
        r.setTitle(q.getTitle());
        r.setDescription(q.getDescription());
        r.setFree(q.isFree());
        r.setPassMark(q.getPassMark());
        r.setActive(q.isActive());
        r.setQuestionCount(questionRepository.countByQuizIdAndDeletedFalse(q.getId()));
        r.setCreatedAt(q.getCreatedAt());
        r.setUpdatedAt(q.getUpdatedAt());
        return r;
    }
}
