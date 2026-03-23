package com.tasklist.server.category.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.tasklist.server.category.dto.CreateCategoryRequest;
import com.tasklist.server.category.dto.UpdateCategoryRequest;
import com.tasklist.server.category.entity.TaskCategoryEntity;
import com.tasklist.server.category.mapper.TaskCategoryMapper;
import com.tasklist.server.category.vo.CategoryResponse;
import com.tasklist.server.common.auth.CurrentUserProvider;
import com.tasklist.server.common.error.BusinessException;
import com.tasklist.server.common.error.ErrorCode;
import com.tasklist.server.task.entity.TaskEntity;
import com.tasklist.server.task.mapper.TaskMapper;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final TaskCategoryMapper taskCategoryMapper;
    private final TaskMapper taskMapper;
    private final CurrentUserProvider currentUserProvider;

    public List<CategoryResponse> listCategories() {
        Long userId = currentUserProvider.requireCurrentUserId();
        return taskCategoryMapper.selectList(new LambdaQueryWrapper<TaskCategoryEntity>()
                        .eq(TaskCategoryEntity::getUserId, userId)
                        .isNull(TaskCategoryEntity::getDeletedAt))
                .stream()
                .sorted(Comparator.comparing(TaskCategoryEntity::getSortNo, Comparator.nullsLast(Integer::compareTo))
                        .thenComparing(TaskCategoryEntity::getId))
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public CategoryResponse createCategory(CreateCategoryRequest request) {
        Long userId = currentUserProvider.requireCurrentUserId();
        LocalDateTime now = LocalDateTime.now();
        TaskCategoryEntity entity = new TaskCategoryEntity();
        entity.setUserId(userId);
        entity.setName(request.name());
        entity.setColor(request.color());
        entity.setSortNo(request.sortNo() == null ? 0 : request.sortNo());
        entity.setIsDefault(Boolean.FALSE);
        entity.setCreatedAt(now);
        entity.setUpdatedAt(now);
        taskCategoryMapper.insert(entity);
        return toResponse(entity);
    }

    @Transactional
    public CategoryResponse updateCategory(Long categoryId, UpdateCategoryRequest request) {
        TaskCategoryEntity entity = requireOwnedCategory(categoryId);
        entity.setName(request.name());
        entity.setColor(request.color());
        entity.setSortNo(request.sortNo() == null ? 0 : request.sortNo());
        entity.setUpdatedAt(LocalDateTime.now());
        taskCategoryMapper.updateById(entity);
        return toResponse(entity);
    }

    @Transactional
    public void deleteCategory(Long categoryId) {
        TaskCategoryEntity entity = requireOwnedCategory(categoryId);
        entity.setDeletedAt(LocalDateTime.now());
        entity.setUpdatedAt(LocalDateTime.now());
        taskCategoryMapper.updateById(entity);

        taskMapper.update(new LambdaUpdateWrapper<TaskEntity>()
                .eq(TaskEntity::getUserId, entity.getUserId())
                .eq(TaskEntity::getCategoryId, categoryId)
                .set(TaskEntity::getCategoryId, null)
                .set(TaskEntity::getUpdatedAt, LocalDateTime.now()));
    }

    public TaskCategoryEntity requireOwnedCategory(Long categoryId) {
        Long userId = currentUserProvider.requireCurrentUserId();
        TaskCategoryEntity entity = taskCategoryMapper.selectOne(new LambdaQueryWrapper<TaskCategoryEntity>()
                .eq(TaskCategoryEntity::getId, categoryId)
                .eq(TaskCategoryEntity::getUserId, userId)
                .isNull(TaskCategoryEntity::getDeletedAt)
                .last("LIMIT 1"));
        if (entity == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "Category not found");
        }
        return entity;
    }

    private CategoryResponse toResponse(TaskCategoryEntity entity) {
        return new CategoryResponse(entity.getId(), entity.getName(), entity.getColor(), entity.getSortNo(), entity.getIsDefault());
    }
}
