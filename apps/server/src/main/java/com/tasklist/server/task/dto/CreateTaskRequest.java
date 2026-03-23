package com.tasklist.server.task.dto;

import com.tasklist.server.common.enums.RepeatType;
import com.tasklist.server.common.enums.TaskPriority;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDateTime;

public record CreateTaskRequest(
        Long categoryId,
        @NotBlank String title,
        String description,
        TaskPriority priority,
        LocalDateTime dueAt,
        LocalDateTime remindAt,
        RepeatType repeatType
) {
}
