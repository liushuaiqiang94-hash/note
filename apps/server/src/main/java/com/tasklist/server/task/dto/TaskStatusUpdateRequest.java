package com.tasklist.server.task.dto;

import com.tasklist.server.common.enums.TaskStatus;
import jakarta.validation.constraints.NotNull;

public record TaskStatusUpdateRequest(@NotNull TaskStatus status) {
}
