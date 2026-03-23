package com.tasklist.server.task.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tasklist.server.category.service.CategoryService;
import com.tasklist.server.common.api.PageResponse;
import com.tasklist.server.common.auth.CurrentUserProvider;
import com.tasklist.server.common.enums.RepeatType;
import com.tasklist.server.common.enums.TaskPriority;
import com.tasklist.server.common.enums.TaskStatus;
import com.tasklist.server.common.error.BusinessException;
import com.tasklist.server.common.error.ErrorCode;
import com.tasklist.server.reminder.service.ReminderJobService;
import com.tasklist.server.task.dto.CreateTaskRequest;
import com.tasklist.server.task.dto.TaskStatusUpdateRequest;
import com.tasklist.server.task.dto.UpdateTaskRequest;
import com.tasklist.server.task.entity.TaskEntity;
import com.tasklist.server.task.mapper.TaskMapper;
import com.tasklist.server.task.vo.TaskResponse;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Comparator;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class TaskService {

    private final TaskMapper taskMapper;
    private final CategoryService categoryService;
    private final ReminderJobService reminderJobService;
    private final CurrentUserProvider currentUserProvider;

    @Transactional
    public TaskResponse createTask(CreateTaskRequest request) {
        Long userId = currentUserProvider.requireCurrentUserId();
        validateCategory(request.categoryId());

        LocalDateTime now = LocalDateTime.now();
        TaskEntity entity = new TaskEntity();
        entity.setUserId(userId);
        entity.setCategoryId(request.categoryId());
        entity.setTitle(request.title());
        entity.setDescription(request.description());
        entity.setPriority(resolvePriority(request.priority()).name());
        entity.setStatus(resolveInitialStatus(request.dueAt()).name());
        entity.setDueAt(request.dueAt());
        entity.setRemindAt(request.remindAt());
        entity.setRepeatType(resolveRepeatType(request.repeatType()).name());
        entity.setCreatedAt(now);
        entity.setUpdatedAt(now);
        taskMapper.insert(entity);
        reminderJobService.rebuildReminderJob(entity);
        return toResponse(entity);
    }

    public PageResponse<TaskResponse> listTasks(String status, Long categoryId, String keyword, LocalDate date, long pageNum, long pageSize) {
        Long userId = currentUserProvider.requireCurrentUserId();
        LambdaQueryWrapper<TaskEntity> wrapper = new LambdaQueryWrapper<TaskEntity>()
                .eq(TaskEntity::getUserId, userId)
                .isNull(TaskEntity::getDeletedAt)
                .orderByDesc(TaskEntity::getCreatedAt);

        if (StringUtils.hasText(status)) {
            wrapper.eq(TaskEntity::getStatus, status.toUpperCase());
        }
        if (categoryId != null) {
            wrapper.eq(TaskEntity::getCategoryId, categoryId);
        }
        if (StringUtils.hasText(keyword)) {
            wrapper.and(condition -> condition.like(TaskEntity::getTitle, keyword)
                    .or()
                    .like(TaskEntity::getDescription, keyword));
        }
        if (date != null) {
            LocalDateTime start = date.atStartOfDay();
            LocalDateTime end = date.plusDays(1).atStartOfDay();
            wrapper.ge(TaskEntity::getDueAt, start).lt(TaskEntity::getDueAt, end);
        }

        Page<TaskEntity> page = taskMapper.selectPage(Page.of(pageNum, pageSize), wrapper);
        return new PageResponse<>(
                page.getCurrent(),
                page.getSize(),
                page.getTotal(),
                page.getRecords().stream().map(this::toResponse).toList()
        );
    }

    public TaskResponse getTask(Long taskId) {
        return toResponse(requireOwnedTask(taskId, false));
    }

    @Transactional
    public TaskResponse updateTask(Long taskId, UpdateTaskRequest request) {
        TaskEntity entity = requireOwnedTask(taskId, false);
        validateCategory(request.categoryId());

        entity.setCategoryId(request.categoryId());
        entity.setTitle(request.title());
        entity.setDescription(request.description());
        entity.setPriority(resolvePriority(request.priority()).name());
        entity.setDueAt(request.dueAt());
        entity.setRemindAt(request.remindAt());
        entity.setRepeatType(resolveRepeatType(request.repeatType()).name());
        if (!TaskStatus.DONE.name().equals(entity.getStatus())) {
            entity.setStatus(resolveInitialStatus(request.dueAt()).name());
        }
        entity.setUpdatedAt(LocalDateTime.now());
        taskMapper.updateById(entity);
        reminderJobService.rebuildReminderJob(entity);
        return toResponse(entity);
    }

    @Transactional
    public TaskResponse updateTaskStatus(Long taskId, TaskStatusUpdateRequest request) {
        TaskEntity entity = requireOwnedTask(taskId, false);
        if (request.status() == TaskStatus.EXPIRED) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "Status EXPIRED is system controlled");
        }

        if (request.status() == TaskStatus.DONE) {
            entity.setStatus(TaskStatus.DONE.name());
            entity.setCompletedAt(LocalDateTime.now());
            reminderJobService.deleteByTaskId(entity.getId());
        } else {
            entity.setStatus(resolveRecoveredStatus(entity.getDueAt()).name());
            entity.setCompletedAt(null);
            reminderJobService.rebuildReminderJob(entity);
        }
        entity.setUpdatedAt(LocalDateTime.now());
        taskMapper.updateById(entity);
        return toResponse(entity);
    }

    @Transactional
    public void deleteTask(Long taskId) {
        TaskEntity entity = requireOwnedTask(taskId, false);
        entity.setDeletedAt(LocalDateTime.now());
        entity.setUpdatedAt(LocalDateTime.now());
        taskMapper.updateById(entity);
        reminderJobService.deleteByTaskId(taskId);
    }

    public List<TaskResponse> listTodayTasks() {
        Long userId = currentUserProvider.requireCurrentUserId();
        LocalDate today = LocalDate.now();
        LocalDateTime start = today.atStartOfDay();
        LocalDateTime end = today.plusDays(1).atStartOfDay();
        Comparator<TaskEntity> comparator = Comparator
                .comparing(TaskEntity::getDueAt, Comparator.nullsLast(LocalDateTime::compareTo))
                .thenComparing((left, right) -> Integer.compare(priorityWeight(right.getPriority()), priorityWeight(left.getPriority())));
        return taskMapper.selectList(new LambdaQueryWrapper<TaskEntity>()
                        .eq(TaskEntity::getUserId, userId)
                        .isNull(TaskEntity::getDeletedAt)
                        .ge(TaskEntity::getDueAt, start)
                        .lt(TaskEntity::getDueAt, end))
                .stream()
                .sorted(comparator)
                .map(this::toResponse)
                .toList();
    }

    public PageResponse<TaskResponse> listRecycleBin(long pageNum, long pageSize) {
        Long userId = currentUserProvider.requireCurrentUserId();
        Page<TaskEntity> page = taskMapper.selectPage(Page.of(pageNum, pageSize), new LambdaQueryWrapper<TaskEntity>()
                .eq(TaskEntity::getUserId, userId)
                .isNotNull(TaskEntity::getDeletedAt)
                .orderByDesc(TaskEntity::getDeletedAt));
        return new PageResponse<>(page.getCurrent(), page.getSize(), page.getTotal(), page.getRecords().stream().map(this::toResponse).toList());
    }

    @Transactional
    public TaskResponse restoreTask(Long taskId) {
        TaskEntity entity = requireOwnedTask(taskId, true);
        if (entity.getDeletedAt() == null) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "Task is not deleted");
        }

        entity.setDeletedAt(null);
        if (TaskStatus.DONE.name().equals(entity.getStatus()) && entity.getCompletedAt() != null) {
            entity.setStatus(TaskStatus.DONE.name());
        } else {
            entity.setStatus(resolveRecoveredStatus(entity.getDueAt()).name());
            entity.setCompletedAt(null);
            reminderJobService.rebuildReminderJob(entity);
        }
        entity.setUpdatedAt(LocalDateTime.now());
        taskMapper.updateById(entity);
        return toResponse(entity);
    }

    @Transactional
    public int markExpiredTasks() {
        List<TaskEntity> overdueTasks = taskMapper.selectList(new LambdaQueryWrapper<TaskEntity>()
                .eq(TaskEntity::getStatus, TaskStatus.TODO.name())
                .isNull(TaskEntity::getDeletedAt)
                .isNotNull(TaskEntity::getDueAt)
                .lt(TaskEntity::getDueAt, LocalDateTime.now()));

        overdueTasks.forEach(task -> {
            task.setStatus(TaskStatus.EXPIRED.name());
            task.setUpdatedAt(LocalDateTime.now());
            taskMapper.updateById(task);
            reminderJobService.deleteByTaskId(task.getId());
        });
        return overdueTasks.size();
    }

    @Transactional
    public int purgeDeletedTasks(int retentionDays) {
        LocalDateTime deadline = LocalDateTime.now().minusDays(retentionDays);
        List<TaskEntity> deletedTasks = taskMapper.selectList(new LambdaQueryWrapper<TaskEntity>()
                .isNotNull(TaskEntity::getDeletedAt)
                .lt(TaskEntity::getDeletedAt, deadline));
        deletedTasks.forEach(task -> taskMapper.deleteById(task.getId()));
        return deletedTasks.size();
    }

    public List<TaskEntity> listActiveTasksForUser(Long userId) {
        return taskMapper.selectList(new LambdaQueryWrapper<TaskEntity>()
                .eq(TaskEntity::getUserId, userId)
                .isNull(TaskEntity::getDeletedAt));
    }

    public TaskEntity requireOwnedTask(Long taskId, boolean includeDeleted) {
        Long userId = currentUserProvider.requireCurrentUserId();
        LambdaQueryWrapper<TaskEntity> wrapper = new LambdaQueryWrapper<TaskEntity>()
                .eq(TaskEntity::getId, taskId)
                .eq(TaskEntity::getUserId, userId)
                .last("LIMIT 1");
        if (!includeDeleted) {
            wrapper.isNull(TaskEntity::getDeletedAt);
        }
        TaskEntity entity = taskMapper.selectOne(wrapper);
        if (entity == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "Task not found");
        }
        return entity;
    }

    private void validateCategory(Long categoryId) {
        if (categoryId != null) {
            categoryService.requireOwnedCategory(categoryId);
        }
    }

    private TaskPriority resolvePriority(TaskPriority priority) {
        return priority == null ? TaskPriority.MEDIUM : priority;
    }

    private RepeatType resolveRepeatType(RepeatType repeatType) {
        return repeatType == null ? RepeatType.NONE : repeatType;
    }

    private TaskStatus resolveInitialStatus(LocalDateTime dueAt) {
        if (dueAt != null && dueAt.isBefore(LocalDateTime.now())) {
            return TaskStatus.EXPIRED;
        }
        return TaskStatus.TODO;
    }

    private TaskStatus resolveRecoveredStatus(LocalDateTime dueAt) {
        if (dueAt != null && dueAt.isBefore(LocalDateTime.now())) {
            return TaskStatus.EXPIRED;
        }
        return TaskStatus.TODO;
    }

    private TaskResponse toResponse(TaskEntity entity) {
        return new TaskResponse(
                entity.getId(),
                entity.getCategoryId(),
                entity.getTitle(),
                entity.getDescription(),
                entity.getPriority(),
                entity.getStatus(),
                entity.getDueAt(),
                entity.getRemindAt(),
                entity.getRepeatType(),
                entity.getCompletedAt(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    private int priorityWeight(String priority) {
        if (TaskPriority.HIGH.name().equals(priority)) {
            return 3;
        }
        if (TaskPriority.MEDIUM.name().equals(priority)) {
            return 2;
        }
        return 1;
    }
}
