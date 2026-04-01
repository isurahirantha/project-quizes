package com.quizapp.module.question.repository;

import com.quizapp.module.question.entity.Question;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface QuestionRepository extends JpaRepository<Question, Long> {

    @Query("SELECT q FROM Question q LEFT JOIN FETCH q.options WHERE q.quiz.id = :quizId AND q.deleted = false ORDER BY q.orderIndex")
    List<Question> findByQuizIdWithOptions(@Param("quizId") Long quizId);

    long countByQuizIdAndDeletedFalse(Long quizId);
}
