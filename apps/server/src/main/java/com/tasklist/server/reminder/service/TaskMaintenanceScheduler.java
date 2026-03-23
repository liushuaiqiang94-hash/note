package com.tasklist.server.reminder.service;

import com.tasklist.server.task.service.TaskService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class TaskMaintenanceScheduler {

    private final TaskService taskService;
    private final ReminderJobService reminderJobService;

    @Scheduled(cron = "0 * * * * *")
    public void expireOverdueTasks() {
        int count = taskService.markExpiredTasks();
        if (count > 0) {
            log.info("Expired {} overdue tasks", count);
        }
    }

    @Scheduled(cron = "0 * * * * *")
    public void promoteReminderJobs() {
        int count = reminderJobService.promoteDueJobs();
        if (count > 0) {
            log.info("Promoted {} reminder jobs", count);
        }
    }

    @Scheduled(cron = "0 0 0 * * *")
    public void purgeDeletedTasks() {
        int count = taskService.purgeDeletedTasks(7);
        if (count > 0) {
            log.info("Purged {} deleted tasks", count);
        }
    }
}
