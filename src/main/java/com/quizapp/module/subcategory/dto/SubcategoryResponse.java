package com.quizapp.module.subcategory.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class SubcategoryResponse {
    private Long id;
    private Long categoryId;
    private String categoryName;
    private String name;
    private String description;
    private boolean active;
    private LocalDateTime createdAt;
}
