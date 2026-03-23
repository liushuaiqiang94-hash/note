package com.tasklist.server.task.entity;

import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;

@Data
@TableName("tasks")
public class TaskEntity {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    @TableField(updateStrategy = FieldStrategy.ALWAYS)
    private Long categoryId;
    private String title;
    @TableField(updateStrategy = FieldStrategy.ALWAYS)
    private String description;
    private String priority;
    private String status;
    @TableField(updateStrategy = FieldStrategy.ALWAYS)
    private LocalDateTime dueAt;
    @TableField(updateStrategy = FieldStrategy.ALWAYS)
    private LocalDateTime remindAt;
    private String repeatType;
    @TableField(updateStrategy = FieldStrategy.ALWAYS)
    private LocalDateTime completedAt;
    @TableField(updateStrategy = FieldStrategy.ALWAYS)
    private LocalDateTime deletedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
