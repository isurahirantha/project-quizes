package com.quizapp.module.quiz.repository;

import com.quizapp.module.quiz.entity.Quiz;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface QuizRepository extends JpaRepository<Quiz, Long> {

    Page<Quiz> findBySubcategoryIdAndDeletedFalse(Long subcategoryId, Pageable pageable);

    @Query("""
            SELECT q FROM Quiz q
            WHERE q.deleted = false
            AND q.active = true
            AND (:subcategoryId IS NULL OR q.subcategory.id = :subcategoryId)
            AND (:isFree IS NULL OR q.isFree = :isFree)
            AND (:search IS NULL OR LOWER(q.title) LIKE LOWER(CONCAT('%', :search, '%')))
            """)
    Page<Quiz> findPublic(
            @Param("subcategoryId") Long subcategoryId,
            @Param("isFree") Boolean isFree,
            @Param("search") String search,
            Pageable pageable
    );

    long countBySubcategoryIdAndDeletedFalse(Long subcategoryId);
}
