package cn.edu.swpu.java_qa_monitor.service.impl;

import cn.edu.swpu.java_qa_monitor.common.dto.ProjectCreateRequest;
import cn.edu.swpu.java_qa_monitor.common.exception.BusinessException;
import cn.edu.swpu.java_qa_monitor.domain.Project;
import cn.edu.swpu.java_qa_monitor.mapper.ProjectMapper;
import cn.edu.swpu.java_qa_monitor.service.ProjectService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;
import java.util.List;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@Service
public class ProjectServiceImpl extends ServiceImpl<ProjectMapper, Project>
        implements ProjectService {

    @Value("${app.upload.base-path}")
    private String uploadBasePath;

    @Override
    public Project createProject(ProjectCreateRequest request, Long userId) {
        String projectDir = uploadBasePath + "/" + userId + "/" + UUID.randomUUID();
        new File(projectDir).mkdirs();

        Project project = new Project();
        project.setUserId(userId);
        project.setName(request.getName());
        project.setDescription(request.getDescription() != null ? request.getDescription() : "");
        project.setSourcePath(projectDir);
        project.setFileCount(0);
        save(project);
        return project;
    }

    @Override
    public List<Project> getProjectsByUserId(Long userId) {
        return list(new LambdaQueryWrapper<Project>()
                .eq(Project::getUserId, userId)
                .orderByDesc(Project::getCreateTime));
    }

    @Override
    public int saveUploadedFiles(Long projectId, MultipartFile[] files) {
        Project project = getById(projectId);
        if (project == null) {
            throw new BusinessException("项目不存在");
        }

        Path targetDir = Paths.get(project.getSourcePath());
        int javaFileCount = 0;

        for (MultipartFile file : files) {
            String originalName = file.getOriginalFilename();
            if (originalName == null) continue;

            try {
                if (originalName.endsWith(".zip")) {
                    javaFileCount += extractZip(file.getInputStream(), targetDir);
                } else if (originalName.endsWith(".java")) {
                    Path targetFile = targetDir.resolve(originalName);
                    file.transferTo(targetFile.toFile());
                    javaFileCount++;
                }
            } catch (IOException e) {
                throw new BusinessException("文件上传失败: " + e.getMessage());
            }
        }

        project.setFileCount(countJavaFiles(targetDir));
        updateById(project);
        return javaFileCount;
    }

    private int extractZip(InputStream inputStream, Path targetDir) throws IOException {
        int count = 0;
        try (ZipInputStream zis = new ZipInputStream(inputStream)) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                if (entry.isDirectory()) continue;
                String name = entry.getName();
                if (!name.endsWith(".java")) continue;

                // Prevent zip slip
                Path resolved = targetDir.resolve(name).normalize();
                if (!resolved.startsWith(targetDir)) continue;

                Files.createDirectories(resolved.getParent());
                Files.copy(zis, resolved, StandardCopyOption.REPLACE_EXISTING);
                count++;
            }
        }
        return count;
    }

    private int countJavaFiles(Path dir) {
        try {
            return (int) Files.walk(dir)
                    .filter(p -> p.toString().endsWith(".java"))
                    .count();
        } catch (IOException e) {
            return 0;
        }
    }
}
