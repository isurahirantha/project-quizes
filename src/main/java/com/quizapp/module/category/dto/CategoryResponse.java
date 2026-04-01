package com.quizapp.module.category.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class CategoryResponse {
    private Long id;
    private String name;
    private String description;
    private BigDecimal price;
    private Integer validityDays;
    private boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
