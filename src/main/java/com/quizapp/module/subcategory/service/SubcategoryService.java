package com.quizapp.module.subcategory.service;

import com.quizapp.exception.ResourceNotFoundException;
import com.quizapp.module.category.entity.Category;
import com.quizapp.module.category.repository.CategoryRepository;
import com.quizapp.module.subcategory.dto.SubcategoryRequest;
import com.quizapp.module.subcategory.dto.SubcategoryResponse;
import com.quizapp.module.subcategory.entity.Subcategory;
import com.quizapp.module.subcategory.repository.SubcategoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class SubcategoryService {

    private final SubcategoryRepository subcategoryRepository;
    private final CategoryRepository categoryRepository;

    @Transactional
    public SubcategoryResponse create(SubcategoryRequest request) {
        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category", request.getCategoryId()));

        Subcategory sub = Subcategory.builder()
                .category(category)
                .name(request.getName())
                .description(request.getDescription())
                .active(request.isActive())
                .deleted(false)
                .build();

        return toResponse(subcategoryRepository.save(sub));
    }

    public List<SubcategoryResponse> listByCategory(Long categoryId) {
        return subcategoryRepository.findByCategoryIdAndActiveTrueAndDeletedFalse(categoryId)
                .stream().map(this::toResponse).toList();
    }

    @Transactional
    public SubcategoryResponse update(Long id, SubcategoryRequest request) {
        Subcategory sub = findActive(id);
        if (!sub.getCategory().getId().equals(request.getCategoryId())) {
            Category category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Category", request.getCategoryId()));
            sub.setCategory(category);
        }
        sub.setName(request.getName());
        sub.setDescription(request.getDescription());
        sub.setActive(request.isActive());
        return toResponse(subcategoryRepository.save(sub));
    }

    @Transactional
    public void delete(Long id) {
        Subcategory sub = findActive(id);
        sub.setDeleted(true);
        subcategoryRepository.save(sub);
    }

    private Subcategory findActive(Long id) {
        Subcategory sub = subcategoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Subcategory", id));
        if (sub.isDeleted()) throw new ResourceNotFoundException("Subcategory", id);
        return sub;
    }

    public SubcategoryResponse toResponse(Subcategory s) {
        SubcategoryResponse r = new SubcategoryResponse();
        r.setId(s.getId());
        r.setCategoryId(s.getCategory().getId());
        r.setCategoryName(s.getCategory().getName());
        r.setName(s.getName());
        r.setDescription(s.getDescription());
        r.setActive(s.isActive());
        r.setCreatedAt(s.getCreatedAt());
        return r;
    }
}
