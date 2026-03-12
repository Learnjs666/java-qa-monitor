package cn.edu.swpu.java_qa_monitor.controller;

import cn.edu.swpu.java_qa_monitor.common.context.UserContext;
import cn.edu.swpu.java_qa_monitor.common.dto.OverviewResponse;
import cn.edu.swpu.java_qa_monitor.common.dto.RadarDataResponse;
import cn.edu.swpu.java_qa_monitor.common.dto.TrendDataResponse;
import cn.edu.swpu.java_qa_monitor.common.result.Result;
import cn.edu.swpu.java_qa_monitor.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/overview")
    public Result<OverviewResponse> getOverview() {
        Long userId = UserContext.get().getUserId();
        OverviewResponse overview = dashboardService.getOverview(userId);
        return Result.success(overview);
    }

    @GetMapping("/health/{projectId}")
    public Result<RadarDataResponse> getHealthRadar(@PathVariable Long projectId) {
        RadarDataResponse radar = dashboardService.getHealthRadar(projectId);
        return Result.success(radar);
    }

    @GetMapping("/trend/{projectId}")
    public Result<TrendDataResponse> getTrend(@PathVariable Long projectId) {
        TrendDataResponse trend = dashboardService.getTrend(projectId);
        return Result.success(trend);
    }
}
