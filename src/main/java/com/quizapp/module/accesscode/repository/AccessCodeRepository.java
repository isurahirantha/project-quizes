package com.quizapp.module.accesscode.repository;

import com.quizapp.module.accesscode.entity.AccessCode;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AccessCodeRepository extends JpaRepository<AccessCode, Long> {
    Optional<AccessCode> findByCodeAndActiveTrue(String code);
    boolean existsByCode(String code);
    Page<AccessCode> findAll(Pageable pageable);
}
