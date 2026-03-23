package com.tasklist.server.stats.service;

import com.tasklist.server.common.auth.CurrentUserProvider;
import com.tasklist.server.common.enums.TaskStatus;
import com.tasklist.server.common.error.BusinessException;
import com.tasklist.server.common.error.ErrorCode;
import com.tasklist.server.stats.vo.StatsOverviewResponse;
import com.tasklist.server.stats.vo.StatsTrendPointResponse;
import com.tasklist.server.task.entity.TaskEntity;
import com.tasklist.server.task.service.TaskService;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class StatsService {

    private final TaskService taskService;
    private final CurrentUserProvider currentUserProvider;

    public StatsOverviewResponse getOverview(String range) {
        RangeInfo rangeInfo = resolveRange(range, true);
        List<TaskEntity> tasks = filterTasks(rangeInfo);
        long totalCount = tasks.size();
        long completedCount = tasks.stream().filter(task -> TaskStatus.DONE.name().equals(task.getStatus())).count();
        double completionRate = totalCount == 0 ? 0D : (double) completedCount / totalCount;
        long streak = calculateCompletionStreak(taskService.listActiveTasksForUser(currentUserProvider.requireCurrentUserId()));
        return new StatsOverviewResponse(completedCount, totalCount, completionRate, streak);
    }

    public List<StatsTrendPointResponse> getTrend(String range) {
        RangeInfo rangeInfo = resolveRange(range, false);
        List<TaskEntity> tasks = filterTasks(rangeInfo);
        Map<LocalDate, Long> completedCountByDate = new HashMap<>();
        tasks.stream()
                .filter(task -> TaskStatus.DONE.name().equals(task.getStatus()))
                .map(this::resolveEffectiveDate)
                .forEach(date -> completedCountByDate.merge(date, 1L, Long::sum));

        List<StatsTrendPointResponse> result = new ArrayList<>();
        LocalDate current = rangeInfo.startDate();
        while (!current.isAfter(rangeInfo.endDate())) {
            result.add(new StatsTrendPointResponse(current.toString(), completedCountByDate.getOrDefault(current, 0L)));
            current = current.plusDays(1);
        }
        return result;
    }

    private List<TaskEntity> filterTasks(RangeInfo rangeInfo) {
        Long userId = currentUserProvider.requireCurrentUserId();
        return taskService.listActiveTasksForUser(userId).stream()
                .filter(task -> {
                    LocalDate effectiveDate = resolveEffectiveDate(task);
                    return !effectiveDate.isBefore(rangeInfo.startDate()) && !effectiveDate.isAfter(rangeInfo.endDate());
                })
                .toList();
    }

    private LocalDate resolveEffectiveDate(TaskEntity task) {
        LocalDateTime dateTime = task.getDueAt() != null ? task.getDueAt() : task.getCreatedAt();
        return dateTime.toLocalDate();
    }

    private long calculateCompletionStreak(List<TaskEntity> tasks) {
        Set<LocalDate> completedDates = tasks.stream()
                .filter(task -> task.getCompletedAt() != null)
                .map(task -> task.getCompletedAt().toLocalDate())
                .collect(java.util.stream.Collectors.toSet());

        LocalDate cursor = LocalDate.now();
        long streak = 0;
        while (completedDates.contains(cursor)) {
            streak++;
            cursor = cursor.minusDays(1);
        }
        return streak;
    }

    private RangeInfo resolveRange(String range, boolean allowToday) {
        if (!StringUtils.hasText(range)) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "Range is required");
        }
        String normalized = range.trim().toLowerCase();
        LocalDate today = LocalDate.now();
        return switch (normalized) {
            case "today" -> {
                if (!allowToday) {
                    throw new BusinessException(ErrorCode.BAD_REQUEST, "Trend only supports week or month");
                }
                yield new RangeInfo(today, today);
            }
            case "week" -> new RangeInfo(today.minusDays(6), today);
            case "month" -> new RangeInfo(today.minusDays(29), today);
            default -> throw new BusinessException(ErrorCode.BAD_REQUEST, "Unsupported range: " + range);
        };
    }

    private record RangeInfo(LocalDate startDate, LocalDate endDate) {
    }
}
