package com.tasklist.server.task.vo;

import java.time.LocalDateTime;

public record TaskResponse(
        Long id,
        Long categoryId,
        String title,
        String description,
        String priority,
        String status,
        LocalDateTime dueAt,
        LocalDateTime remindAt,
        String repeatType,
        LocalDateTime completedAt,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
