package com.quizapp.module.accesscode.repository;

import com.quizapp.module.accesscode.entity.AccessCodeUsage;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AccessCodeUsageRepository extends JpaRepository<AccessCodeUsage, Long> {
}
