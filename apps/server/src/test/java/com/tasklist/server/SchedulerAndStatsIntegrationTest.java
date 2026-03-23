package com.tasklist.server;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.tasklist.server.common.enums.ReminderJobStatus;
import com.tasklist.server.common.enums.RepeatType;
import com.tasklist.server.common.enums.TaskPriority;
import com.tasklist.server.common.enums.TaskStatus;
import com.tasklist.server.reminder.entity.TaskReminderJobEntity;
import com.tasklist.server.reminder.mapper.TaskReminderJobMapper;
import com.tasklist.server.reminder.service.TaskMaintenanceScheduler;
import com.tasklist.server.task.entity.TaskEntity;
import com.tasklist.server.task.mapper.TaskMapper;
import com.tasklist.server.user.entity.UserEntity;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class SchedulerAndStatsIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private TaskMapper taskMapper;

    @Autowired
    private TaskReminderJobMapper taskReminderJobMapper;

    @Autowired
    private TaskMaintenanceScheduler taskMaintenanceScheduler;

    @Test
    void shouldExpireTasksPromoteRemindersAndReturnStats() throws Exception {
        UserEntity user = createUser("openid-scheduler-1", "SchedulerUser");
        String authHeader = bearerToken(user);
        LocalDateTime now = LocalDateTime.now();

        TaskEntity overdueTask = new TaskEntity();
        overdueTask.setUserId(user.getId());
        overdueTask.setTitle("过期任务");
        overdueTask.setDescription("待过期");
        overdueTask.setPriority(TaskPriority.MEDIUM.name());
        overdueTask.setStatus(TaskStatus.TODO.name());
        overdueTask.setRepeatType(RepeatType.NONE.name());
        overdueTask.setDueAt(now.minusHours(2));
        overdueTask.setCreatedAt(now.minusDays(1));
        overdueTask.setUpdatedAt(now.minusDays(1));
        taskMapper.insert(overdueTask);

        TaskEntity reminderTask = new TaskEntity();
        reminderTask.setUserId(user.getId());
        reminderTask.setTitle("提醒任务");
        reminderTask.setDescription("待提醒");
        reminderTask.setPriority(TaskPriority.LOW.name());
        reminderTask.setStatus(TaskStatus.TODO.name());
        reminderTask.setRepeatType(RepeatType.NONE.name());
        reminderTask.setDueAt(now.plusHours(3));
        reminderTask.setCreatedAt(now.minusHours(1));
        reminderTask.setUpdatedAt(now.minusHours(1));
        taskMapper.insert(reminderTask);

        TaskReminderJobEntity reminderJob = new TaskReminderJobEntity();
        reminderJob.setTaskId(reminderTask.getId());
        reminderJob.setUserId(user.getId());
        reminderJob.setPlannedAt(now.minusMinutes(10));
        reminderJob.setStatus(ReminderJobStatus.PENDING.name());
        reminderJob.setCreatedAt(now.minusMinutes(20));
        reminderJob.setUpdatedAt(now.minusMinutes(20));
        taskReminderJobMapper.insert(reminderJob);

        TaskEntity completedTask = new TaskEntity();
        completedTask.setUserId(user.getId());
        completedTask.setTitle("已完成任务");
        completedTask.setDescription("用于统计");
        completedTask.setPriority(TaskPriority.HIGH.name());
        completedTask.setStatus(TaskStatus.DONE.name());
        completedTask.setRepeatType(RepeatType.NONE.name());
        completedTask.setDueAt(now);
        completedTask.setCompletedAt(now);
        completedTask.setCreatedAt(now.minusDays(1));
        completedTask.setUpdatedAt(now);
        taskMapper.insert(completedTask);

        TaskEntity deletedTask = new TaskEntity();
        deletedTask.setUserId(user.getId());
        deletedTask.setTitle("待清理任务");
        deletedTask.setDescription("回收站");
        deletedTask.setPriority(TaskPriority.LOW.name());
        deletedTask.setStatus(TaskStatus.TODO.name());
        deletedTask.setRepeatType(RepeatType.NONE.name());
        deletedTask.setCreatedAt(now.minusDays(10));
        deletedTask.setUpdatedAt(now.minusDays(10));
        deletedTask.setDeletedAt(now.minusDays(8));
        taskMapper.insert(deletedTask);

        taskMaintenanceScheduler.expireOverdueTasks();
        taskMaintenanceScheduler.promoteReminderJobs();
        taskMaintenanceScheduler.purgeDeletedTasks();

        TaskEntity updatedOverdueTask = taskMapper.selectById(overdueTask.getId());
        TaskReminderJobEntity updatedReminderJob = taskReminderJobMapper.selectById(reminderJob.getId());

        org.assertj.core.api.Assertions.assertThat(updatedOverdueTask.getStatus()).isEqualTo(TaskStatus.EXPIRED.name());
        org.assertj.core.api.Assertions.assertThat(updatedReminderJob.getStatus()).isEqualTo(ReminderJobStatus.READY.name());
        org.assertj.core.api.Assertions.assertThat(taskMapper.selectById(deletedTask.getId())).isNull();

        mockMvc.perform(get("/api/app/v1/stats/overview")
                        .header("Authorization", authHeader)
                        .param("range", "month"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalCount", is(3)))
                .andExpect(jsonPath("$.data.completedCount", is(1)));

        mockMvc.perform(get("/api/app/v1/stats/trend")
                        .header("Authorization", authHeader)
                        .param("range", "week"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()", is(7)));
    }
}
