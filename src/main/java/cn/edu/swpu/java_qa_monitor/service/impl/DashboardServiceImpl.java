package cn.edu.swpu.java_qa_monitor.service.impl;

import cn.edu.swpu.java_qa_monitor.common.dto.OverviewResponse;
import cn.edu.swpu.java_qa_monitor.common.dto.RadarDataResponse;
import cn.edu.swpu.java_qa_monitor.common.dto.TrendDataResponse;
import cn.edu.swpu.java_qa_monitor.domain.Project;
import cn.edu.swpu.java_qa_monitor.domain.ScanIssue;
import cn.edu.swpu.java_qa_monitor.domain.ScanTask;
import cn.edu.swpu.java_qa_monitor.service.*;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    private final ProjectService projectService;
    private final ScanTaskService scanTaskService;
    private final ScanIssueService scanIssueService;

    @Override
    public OverviewResponse getOverview(Long userId) {
        OverviewResponse resp = new OverviewResponse();

        List<Project> projects = projectService.getProjectsByUserId(userId);
        resp.setTotalProjects(projects.size());

        List<Long> projectIds = projects.stream().map(Project::getId).collect(Collectors.toList());
        if (projectIds.isEmpty()) {
            return resp;
        }

        long totalScans = scanTaskService.count(new LambdaQueryWrapper<ScanTask>()
                .in(ScanTask::getProjectId, projectIds));
        resp.setTotalScans(totalScans);

        long totalIssues = scanIssueService.count(new LambdaQueryWrapper<ScanIssue>()
                .in(ScanIssue::getProjectId, projectIds));
        resp.setTotalIssues(totalIssues);

        long errorCount = scanIssueService.count(new LambdaQueryWrapper<ScanIssue>()
                .in(ScanIssue::getProjectId, projectIds)
                .eq(ScanIssue::getSeverity, "ERROR"));
        resp.setErrorCount(errorCount);

        long warningCount = scanIssueService.count(new LambdaQueryWrapper<ScanIssue>()
                .in(ScanIssue::getProjectId, projectIds)
                .eq(ScanIssue::getSeverity, "WARNING"));
        resp.setWarningCount(warningCount);

        long infoCount = scanIssueService.count(new LambdaQueryWrapper<ScanIssue>()
                .in(ScanIssue::getProjectId, projectIds)
                .eq(ScanIssue::getSeverity, "INFO"));
        resp.setInfoCount(infoCount);

        return resp;
    }

    @Override
    public RadarDataResponse getHealthRadar(Long projectId) {
        RadarDataResponse resp = new RadarDataResponse();

        // Get the latest completed task for this project
        ScanTask latestTask = scanTaskService.getOne(new LambdaQueryWrapper<ScanTask>()
                .eq(ScanTask::getProjectId, projectId)
                .eq(ScanTask::getStatus, "COMPLETED")
                .orderByDesc(ScanTask::getCreateTime)
                .last("LIMIT 1"));

        if (latestTask == null) {
            resp.setScores(List.of(
                    new RadarDataResponse.DimensionScore("命名规范", 100),
                    new RadarDataResponse.DimensionScore("复杂度", 100),
                    new RadarDataResponse.DimensionScore("代码风格", 100),
                    new RadarDataResponse.DimensionScore("文档注释", 100),
                    new RadarDataResponse.DimensionScore("最佳实践", 100),
                    new RadarDataResponse.DimensionScore("综合评分", 100)
            ));
            return resp;
        }

        List<ScanIssue> issues = scanIssueService.getByTaskId(latestTask.getId());

        Map<String, List<ScanIssue>> byCategory = new HashMap<>();
        for (ScanIssue issue : issues) {
            String category = getCategoryByRuleCode(issue.getRuleCode());
            byCategory.computeIfAbsent(category, k -> new ArrayList<>()).add(issue);
        }

        int namingScore = calculateScore(byCategory.getOrDefault("NAMING", List.of()));
        int complexityScore = calculateScore(byCategory.getOrDefault("COMPLEXITY", List.of()));
        int styleScore = calculateScore(byCategory.getOrDefault("STYLE", List.of()));
        int docScore = calculateScore(byCategory.getOrDefault("DOC", List.of()));
        int bestPracticeScore = calculateScore(byCategory.getOrDefault("BEST_PRACTICE", List.of()));
        int overallScore = (namingScore + complexityScore + styleScore + docScore + bestPracticeScore) / 5;

        resp.setScores(List.of(
                new RadarDataResponse.DimensionScore("命名规范", namingScore),
                new RadarDataResponse.DimensionScore("复杂度", complexityScore),
                new RadarDataResponse.DimensionScore("代码风格", styleScore),
                new RadarDataResponse.DimensionScore("文档注释", docScore),
                new RadarDataResponse.DimensionScore("最佳实践", bestPracticeScore),
                new RadarDataResponse.DimensionScore("综合评分", overallScore)
        ));
        return resp;
    }

    @Override
    public TrendDataResponse getTrend(Long projectId) {
        TrendDataResponse resp = new TrendDataResponse();

        List<ScanTask> tasks = scanTaskService.list(new LambdaQueryWrapper<ScanTask>()
                .eq(ScanTask::getProjectId, projectId)
                .eq(ScanTask::getStatus, "COMPLETED")
                .orderByAsc(ScanTask::getCreateTime)
                .last("LIMIT 20"));

        SimpleDateFormat sdf = new SimpleDateFormat("MM-dd HH:mm");
        List<TrendDataResponse.TrendPoint> points = new ArrayList<>();

        for (ScanTask task : tasks) {
            List<ScanIssue> issues = scanIssueService.getByTaskId(task.getId());
            long errors = issues.stream().filter(i -> "ERROR".equals(i.getSeverity())).count();
            long warnings = issues.stream().filter(i -> "WARNING".equals(i.getSeverity())).count();
            long infos = issues.stream().filter(i -> "INFO".equals(i.getSeverity())).count();

            points.add(new TrendDataResponse.TrendPoint(
                    sdf.format(task.getCreateTime()), errors, warnings, infos));
        }

        resp.setPoints(points);
        return resp;
    }

    private String getCategoryByRuleCode(String ruleCode) {
        return switch (ruleCode) {
            case "CLASS_NAME_CONVENTION", "METHOD_NAME_CONVENTION", "FIELD_NAME_CONVENTION" -> "NAMING";
            case "HIGH_CYCLOMATIC" -> "COMPLEXITY";
            case "METHOD_TOO_LONG", "CLASS_TOO_LONG", "PARAM_COUNT_EXCEEDED" -> "STYLE";
            case "MISSING_JAVADOC" -> "DOC";
            case "EMPTY_CATCH_BLOCK", "MAGIC_NUMBER" -> "BEST_PRACTICE";
            default -> "STYLE";
        };
    }

    private int calculateScore(List<ScanIssue> issues) {
        if (issues.isEmpty()) return 100;
        int penalty = 0;
        for (ScanIssue issue : issues) {
            switch (issue.getSeverity()) {
                case "ERROR" -> penalty += 10;
                case "WARNING" -> penalty += 5;
                case "INFO" -> penalty += 2;
            }
        }
        return Math.max(0, 100 - penalty);
    }
}
