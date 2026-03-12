package cn.edu.swpu.java_qa_monitor.controller;

import cn.edu.swpu.java_qa_monitor.common.context.UserContext;
import cn.edu.swpu.java_qa_monitor.common.dto.ProjectCreateRequest;
import cn.edu.swpu.java_qa_monitor.common.result.Result;
import cn.edu.swpu.java_qa_monitor.domain.Project;
import cn.edu.swpu.java_qa_monitor.service.ProjectService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/project")
@RequiredArgsConstructor
public class ProjectController {

    private final ProjectService projectService;

    @PostMapping
    public Result<Project> createProject(@Valid @RequestBody ProjectCreateRequest request) {
        Long userId = UserContext.get().getUserId();
        Project project = projectService.createProject(request, userId);
        return Result.success(project);
    }

    @GetMapping("/list")
    public Result<List<Project>> getProjectList() {
        Long userId = UserContext.get().getUserId();
        List<Project> list = projectService.getProjectsByUserId(userId);
        return Result.success(list);
    }

    @GetMapping("/{id}")
    public Result<Project> getProject(@PathVariable Long id) {
        Project project = projectService.getById(id);
        return Result.success(project);
    }

    @DeleteMapping("/{id}")
    public Result<Void> deleteProject(@PathVariable Long id) {
        projectService.removeById(id);
        return Result.success(null);
    }

    @PostMapping("/{id}/upload")
    public Result<Integer> uploadFiles(@PathVariable Long id,
                                       @RequestParam("files") MultipartFile[] files) {
        int count = projectService.saveUploadedFiles(id, files);
        return Result.success(count);
    }
}
