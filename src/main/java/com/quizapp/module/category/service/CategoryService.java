package com.quizapp.module.category.service;

import com.quizapp.common.PageResponse;
import com.quizapp.exception.BadRequestException;
import com.quizapp.exception.ResourceNotFoundException;
import com.quizapp.module.category.dto.CategoryRequest;
import com.quizapp.module.category.dto.CategoryResponse;
import com.quizapp.module.category.entity.Category;
import com.quizapp.module.category.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;

    @Transactional
    public CategoryResponse create(CategoryRequest request) {
        if (categoryRepository.existsByNameAndDeletedFalse(request.getName())) {
            throw new BadRequestException("Category with name '" + request.getName() + "' already exists");
        }

        Category category = Category.builder()
                .name(request.getName())
                .description(request.getDescription())
                .price(request.getPrice())
                .validityDays(request.getValidityDays())
                .active(request.isActive())
                .deleted(false)
                .build();

        return toResponse(categoryRepository.save(category));
    }

    public PageResponse<CategoryResponse> listPublic(int page, int size) {
        var pageable = PageRequest.of(page, size, Sort.by("name").ascending());
        return PageResponse.of(categoryRepository.findByActiveTrueAndDeletedFalse(pageable).map(this::toResponse));
    }

    public CategoryResponse getById(Long id) {
        return toResponse(findActive(id));
    }

    @Transactional
    public CategoryResponse update(Long id, CategoryRequest request) {
        Category category = findActive(id);
        category.setName(request.getName());
        category.setDescription(request.getDescription());
        category.setPrice(request.getPrice());
        category.setValidityDays(request.getValidityDays());
        category.setActive(request.isActive());
        return toResponse(categoryRepository.save(category));
    }

    @Transactional
    public void delete(Long id) {
        Category category = findActive(id);
        category.setDeleted(true);
        categoryRepository.save(category);
        log.info("Soft deleted category id: {}", id);
    }

    private Category findActive(Long id) {
        Category cat = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category", id));
        if (cat.isDeleted()) throw new ResourceNotFoundException("Category", id);
        return cat;
    }

    public CategoryResponse toResponse(Category c) {
        CategoryResponse r = new CategoryResponse();
        r.setId(c.getId());
        r.setName(c.getName());
        r.setDescription(c.getDescription());
        r.setPrice(c.getPrice());
        r.setValidityDays(c.getValidityDays());
        r.setActive(c.isActive());
        r.setCreatedAt(c.getCreatedAt());
        r.setUpdatedAt(c.getUpdatedAt());
        return r;
    }
}
