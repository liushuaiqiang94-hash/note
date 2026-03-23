package com.tasklist.server.stats.vo;

public record StatsOverviewResponse(long completedCount, long totalCount, double completionRate, long consecutiveCompletedDays) {
}
