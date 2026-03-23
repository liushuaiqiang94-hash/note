package com.tasklist.server.category.vo;

public record CategoryResponse(Long id, String name, String color, Integer sortNo, Boolean isDefault) {
}
