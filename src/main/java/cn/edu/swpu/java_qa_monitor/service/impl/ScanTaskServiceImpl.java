package cn.edu.swpu.java_qa_monitor.service.impl;

import cn.edu.swpu.java_qa_monitor.common.dto.ScanTaskResponse;
import cn.edu.swpu.java_qa_monitor.common.exception.BusinessException;
import cn.edu.swpu.java_qa_monitor.domain.Project;
import cn.edu.swpu.java_qa_monitor.domain.ScanTask;
import cn.edu.swpu.java_qa_monitor.engine.CodeAnalysisService;
import cn.edu.swpu.java_qa_monitor.mapper.ScanTaskMapper;
import cn.edu.swpu.java_qa_monitor.service.ProjectService;
import cn.edu.swpu.java_qa_monitor.service.ScanTaskService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ScanTaskServiceImpl extends ServiceImpl<ScanTaskMapper, ScanTask>
        implements ScanTaskService {

    private final ProjectService projectService;
    private final CodeAnalysisService codeAnalysisService;

    @Override
    public ScanTask createAndRunScan(Long projectId, Long userId) {
        Project project = projectService.getById(projectId);
        if (project == null) {
            throw new BusinessException("项目不存在");
        }

        ScanTask task = new ScanTask();
        task.setProjectId(projectId);
        task.setUserId(userId);
        task.setStatus("PENDING");
        task.setIssueCount(0);
        task.setFileCount(0);
        save(task);

        // Async execution
        codeAnalysisService.runAnalysis(task.getId(), project);

        return task;
    }

    @Override
    public List<ScanTaskResponse> getTasksByProjectId(Long projectId) {
        Project project = projectService.getById(projectId);
        String projectName = project != null ? project.getName() : "";

        List<ScanTask> tasks = list(new LambdaQueryWrapper<ScanTask>()
                .eq(ScanTask::getProjectId, projectId)
                .orderByDesc(ScanTask::getCreateTime));

        return tasks.stream().map(t -> {
            ScanTaskResponse resp = new ScanTaskResponse();
            resp.setId(t.getId());
            resp.setProjectId(t.getProjectId());
            resp.setProjectName(projectName);
            resp.setStatus(t.getStatus());
            resp.setIssueCount(t.getIssueCount());
            resp.setFileCount(t.getFileCount());
            resp.setErrorMessage(t.getErrorMessage());
            resp.setCreateTime(t.getCreateTime());
            return resp;
        }).collect(Collectors.toList());
    }
}
