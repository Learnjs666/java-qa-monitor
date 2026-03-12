package cn.edu.swpu.java_qa_monitor.service;

import cn.edu.swpu.java_qa_monitor.common.dto.OverviewResponse;
import cn.edu.swpu.java_qa_monitor.common.dto.RadarDataResponse;
import cn.edu.swpu.java_qa_monitor.common.dto.TrendDataResponse;

public interface DashboardService {

    OverviewResponse getOverview(Long userId);

    RadarDataResponse getHealthRadar(Long projectId);

    TrendDataResponse getTrend(Long projectId);
}
