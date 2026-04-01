package com.quizapp.module.category.controller;

import com.quizapp.common.ApiResponse;
import com.quizapp.common.PageResponse;
import com.quizapp.module.category.dto.CategoryRequest;
import com.quizapp.module.category.dto.CategoryResponse;
import com.quizapp.module.category.service.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@Tag(name = "Category", description = "Category management")
public class CategoryController {

    private final CategoryService categoryService;

    @PostMapping("/api/admin/categories")
    @Operation(summary = "Create category [Admin]", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<CategoryResponse>> create(@Valid @RequestBody CategoryRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Category created", categoryService.create(request)));
    }

    @GetMapping("/api/categories")
    @Operation(summary = "List all active categories [Public]")
    public ResponseEntity<ApiResponse<PageResponse<CategoryResponse>>> listAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ApiResponse.ok(categoryService.listPublic(page, size)));
    }

    @GetMapping("/api/categories/{id}")
    @Operation(summary = "Get category by ID [Public]")
    public ResponseEntity<ApiResponse<CategoryResponse>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(categoryService.getById(id)));
    }

    @PutMapping("/api/admin/categories/{id}")
    @Operation(summary = "Update category [Admin]", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<CategoryResponse>> update(
            @PathVariable Long id, @Valid @RequestBody CategoryRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Category updated", categoryService.update(id, request)));
    }

    @DeleteMapping("/api/admin/categories/{id}")
    @Operation(summary = "Delete category (soft) [Admin]", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        categoryService.delete(id);
        return ResponseEntity.ok(ApiResponse.ok("Category deleted", null));
    }
}
