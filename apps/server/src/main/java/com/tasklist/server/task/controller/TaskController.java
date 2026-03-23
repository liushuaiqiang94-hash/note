package com.tasklist.server.task.controller;

import com.tasklist.server.common.api.ApiResponse;
import com.tasklist.server.common.api.PageResponse;
import com.tasklist.server.task.dto.CreateTaskRequest;
import com.tasklist.server.task.dto.TaskStatusUpdateRequest;
import com.tasklist.server.task.dto.UpdateTaskRequest;
import com.tasklist.server.task.service.TaskService;
import com.tasklist.server.task.vo.TaskResponse;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/app/v1/tasks")
public class TaskController {

    private final TaskService taskService;

    @GetMapping
    public ApiResponse<PageResponse<TaskResponse>> listTasks(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(defaultValue = "1") long pageNum,
            @RequestParam(defaultValue = "10") long pageSize
    ) {
        return ApiResponse.success(taskService.listTasks(status, categoryId, keyword, date, pageNum, pageSize));
    }

    @PostMapping
    public ApiResponse<TaskResponse> createTask(@Valid @RequestBody CreateTaskRequest request) {
        return ApiResponse.success(taskService.createTask(request));
    }

    @GetMapping("/{taskId}")
    public ApiResponse<TaskResponse> getTask(@PathVariable Long taskId) {
        return ApiResponse.success(taskService.getTask(taskId));
    }

    @PutMapping("/{taskId}")
    public ApiResponse<TaskResponse> updateTask(@PathVariable Long taskId, @Valid @RequestBody UpdateTaskRequest request) {
        return ApiResponse.success(taskService.updateTask(taskId, request));
    }

    @PatchMapping("/{taskId}/status")
    public ApiResponse<TaskResponse> updateStatus(@PathVariable Long taskId, @Valid @RequestBody TaskStatusUpdateRequest request) {
        return ApiResponse.success(taskService.updateTaskStatus(taskId, request));
    }

    @DeleteMapping("/{taskId}")
    public ApiResponse<Void> deleteTask(@PathVariable Long taskId) {
        taskService.deleteTask(taskId);
        return ApiResponse.success();
    }

    @GetMapping("/today")
    public ApiResponse<List<TaskResponse>> listTodayTasks() {
        return ApiResponse.success(taskService.listTodayTasks());
    }

    @GetMapping("/recycle-bin")
    public ApiResponse<PageResponse<TaskResponse>> listRecycleBin(
            @RequestParam(defaultValue = "1") long pageNum,
            @RequestParam(defaultValue = "10") long pageSize
    ) {
        return ApiResponse.success(taskService.listRecycleBin(pageNum, pageSize));
    }

    @PostMapping("/{taskId}/restore")
    public ApiResponse<TaskResponse> restoreTask(@PathVariable Long taskId) {
        return ApiResponse.success(taskService.restoreTask(taskId));
    }
}
