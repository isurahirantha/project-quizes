package com.quizapp.module.subcategory.repository;

import com.quizapp.module.subcategory.entity.Subcategory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SubcategoryRepository extends JpaRepository<Subcategory, Long> {
    List<Subcategory> findByCategoryIdAndActiveTrueAndDeletedFalse(Long categoryId);
    Page<Subcategory> findByCategoryIdAndDeletedFalse(Long categoryId, Pageable pageable);
}
