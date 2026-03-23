package com.tasklist.server.reminder.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.tasklist.server.common.enums.ReminderJobStatus;
import com.tasklist.server.common.enums.TaskStatus;
import com.tasklist.server.reminder.entity.TaskReminderJobEntity;
import com.tasklist.server.reminder.mapper.TaskReminderJobMapper;
import com.tasklist.server.task.entity.TaskEntity;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReminderJobService {

    private final TaskReminderJobMapper taskReminderJobMapper;

    @Transactional
    public void rebuildReminderJob(TaskEntity task) {
        deleteByTaskId(task.getId());
        if (task.getRemindAt() == null || task.getDeletedAt() != null || TaskStatus.DONE.name().equals(task.getStatus())) {
            return;
        }

        LocalDateTime now = LocalDateTime.now();
        TaskReminderJobEntity entity = new TaskReminderJobEntity();
        entity.setTaskId(task.getId());
        entity.setUserId(task.getUserId());
        entity.setPlannedAt(task.getRemindAt());
        entity.setStatus(ReminderJobStatus.PENDING.name());
        entity.setCreatedAt(now);
        entity.setUpdatedAt(now);
        taskReminderJobMapper.insert(entity);
    }

    @Transactional
    public void deleteByTaskId(Long taskId) {
        taskReminderJobMapper.delete(new LambdaQueryWrapper<TaskReminderJobEntity>()
                .eq(TaskReminderJobEntity::getTaskId, taskId));
    }

    @Transactional
    public int promoteDueJobs() {
        List<TaskReminderJobEntity> jobs = taskReminderJobMapper.selectList(new LambdaQueryWrapper<TaskReminderJobEntity>()
                .eq(TaskReminderJobEntity::getStatus, ReminderJobStatus.PENDING.name())
                .le(TaskReminderJobEntity::getPlannedAt, LocalDateTime.now()));
        jobs.forEach(job -> {
            job.setStatus(ReminderJobStatus.READY.name());
            job.setUpdatedAt(LocalDateTime.now());
            taskReminderJobMapper.updateById(job);
        });
        if (!jobs.isEmpty()) {
            log.info("Promoted {} reminder jobs to READY", jobs.size());
        }
        return jobs.size();
    }

    @Transactional
    public void markReadyJobsAsSent() {
        taskReminderJobMapper.update(null, new LambdaUpdateWrapper<TaskReminderJobEntity>()
                .eq(TaskReminderJobEntity::getStatus, ReminderJobStatus.READY.name())
                .set(TaskReminderJobEntity::getStatus, ReminderJobStatus.SENT.name())
                .set(TaskReminderJobEntity::getSentAt, LocalDateTime.now())
                .set(TaskReminderJobEntity::getUpdatedAt, LocalDateTime.now()));
    }
}
