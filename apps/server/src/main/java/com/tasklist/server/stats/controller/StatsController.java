package com.tasklist.server.stats.controller;

import com.tasklist.server.common.api.ApiResponse;
import com.tasklist.server.stats.service.StatsService;
import com.tasklist.server.stats.vo.StatsOverviewResponse;
import com.tasklist.server.stats.vo.StatsTrendPointResponse;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/app/v1/stats")
public class StatsController {

    private final StatsService statsService;

    @GetMapping("/overview")
    public ApiResponse<StatsOverviewResponse> getOverview(@RequestParam String range) {
        return ApiResponse.success(statsService.getOverview(range));
    }

    @GetMapping("/trend")
    public ApiResponse<List<StatsTrendPointResponse>> getTrend(@RequestParam String range) {
        return ApiResponse.success(statsService.getTrend(range));
    }
}
