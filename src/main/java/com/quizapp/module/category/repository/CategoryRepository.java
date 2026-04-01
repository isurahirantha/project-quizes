package com.quizapp.module.category.repository;

import com.quizapp.module.category.entity.Category;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<Category, Long> {
    Page<Category> findByActiveTrueAndDeletedFalse(Pageable pageable);
    boolean existsByNameAndDeletedFalse(String name);
}
