package cn.edu.swpu.java_qa_monitor.controller;

import cn.edu.swpu.java_qa_monitor.common.context.UserContext;
import cn.edu.swpu.java_qa_monitor.common.dto.ScanTaskResponse;
import cn.edu.swpu.java_qa_monitor.common.result.Result;
import cn.edu.swpu.java_qa_monitor.domain.ScanIssue;
import cn.edu.swpu.java_qa_monitor.domain.ScanMetric;
import cn.edu.swpu.java_qa_monitor.domain.ScanTask;
import cn.edu.swpu.java_qa_monitor.service.ScanIssueService;
import cn.edu.swpu.java_qa_monitor.service.ScanMetricService;
import cn.edu.swpu.java_qa_monitor.service.ScanTaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/scan")
@RequiredArgsConstructor
public class ScanController {

    private final ScanTaskService scanTaskService;
    private final ScanIssueService scanIssueService;
    private final ScanMetricService scanMetricService;

    @PostMapping("/run/{projectId}")
    public Result<ScanTask> runScan(@PathVariable Long projectId) {
        Long userId = UserContext.get().getUserId();
        ScanTask task = scanTaskService.createAndRunScan(projectId, userId);
        return Result.success(task);
    }

    @GetMapping("/task/list/{projectId}")
    public Result<List<ScanTaskResponse>> getTaskList(@PathVariable Long projectId) {
        List<ScanTaskResponse> list = scanTaskService.getTasksByProjectId(projectId);
        return Result.success(list);
    }

    @GetMapping("/task/{taskId}")
    public Result<ScanTask> getTask(@PathVariable Long taskId) {
        ScanTask task = scanTaskService.getById(taskId);
        return Result.success(task);
    }

    @GetMapping("/issues/{taskId}")
    public Result<List<ScanIssue>> getIssues(@PathVariable Long taskId) {
        List<ScanIssue> issues = scanIssueService.getByTaskId(taskId);
        return Result.success(issues);
    }

    @GetMapping("/metrics/{taskId}")
    public Result<List<ScanMetric>> getMetrics(@PathVariable Long taskId) {
        List<ScanMetric> metrics = scanMetricService.getByTaskId(taskId);
        return Result.success(metrics);
    }
}
