package com.quizapp.module.subcategory.controller;

import com.quizapp.common.ApiResponse;
import com.quizapp.module.subcategory.dto.SubcategoryRequest;
import com.quizapp.module.subcategory.dto.SubcategoryResponse;
import com.quizapp.module.subcategory.service.SubcategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Tag(name = "Subcategory", description = "Subcategory management")
public class SubcategoryController {

    private final SubcategoryService subcategoryService;

    @PostMapping("/api/admin/subcategories")
    @Operation(summary = "Create subcategory [Admin]", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<SubcategoryResponse>> create(@Valid @RequestBody SubcategoryRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Subcategory created", subcategoryService.create(request)));
    }

    @GetMapping("/api/subcategories")
    @Operation(summary = "List subcategories by category [Public]")
    public ResponseEntity<ApiResponse<List<SubcategoryResponse>>> listByCategory(
            @RequestParam Long categoryId) {
        return ResponseEntity.ok(ApiResponse.ok(subcategoryService.listByCategory(categoryId)));
    }

    @PutMapping("/api/admin/subcategories/{id}")
    @Operation(summary = "Update subcategory [Admin]", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<SubcategoryResponse>> update(
            @PathVariable Long id, @Valid @RequestBody SubcategoryRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Updated", subcategoryService.update(id, request)));
    }

    @DeleteMapping("/api/admin/subcategories/{id}")
    @Operation(summary = "Delete subcategory (soft) [Admin]", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        subcategoryService.delete(id);
        return ResponseEntity.ok(ApiResponse.ok("Deleted", null));
    }
}
