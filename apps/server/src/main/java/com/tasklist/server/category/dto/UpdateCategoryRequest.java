package com.tasklist.server.category.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateCategoryRequest(
        @NotBlank String name,
        @Size(max = 16) String color,
        Integer sortNo
) {
}
