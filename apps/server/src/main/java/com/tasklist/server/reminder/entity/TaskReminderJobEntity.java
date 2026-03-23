package com.tasklist.server.reminder.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;

@Data
@TableName("task_reminder_jobs")
public class TaskReminderJobEntity {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long taskId;
    private Long userId;
    private LocalDateTime plannedAt;
    private String status;
    private LocalDateTime sentAt;
    private String failReason;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
