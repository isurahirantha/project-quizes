package com.quizapp.module.admin.repository;

import com.quizapp.module.admin.entity.Admin;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AdminRepository extends JpaRepository<Admin, Long> {
    Optional<Admin> findByEmailAndActiveTrue(String email);
    Optional<Admin> findByEmail(String email);
    boolean existsByEmail(String email);
}
