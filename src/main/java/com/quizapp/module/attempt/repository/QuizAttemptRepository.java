package com.quizapp.module.attempt.repository;

import com.quizapp.module.attempt.entity.QuizAttempt;
import com.quizapp.common.enums.AttemptStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface QuizAttemptRepository extends JpaRepository<QuizAttempt, Long> {
    Optional<QuizAttempt> findByIdAndStatus(Long id, AttemptStatus status);
    long countByQuizIdAndDeviceIdAndStatus(Long quizId, String deviceId, AttemptStatus status);
}
