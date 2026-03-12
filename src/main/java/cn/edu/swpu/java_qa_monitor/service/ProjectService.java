package cn.edu.swpu.java_qa_monitor.service;

import cn.edu.swpu.java_qa_monitor.common.dto.ProjectCreateRequest;
import cn.edu.swpu.java_qa_monitor.domain.Project;
import com.baomidou.mybatisplus.extension.service.IService;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ProjectService extends IService<Project> {

    Project createProject(ProjectCreateRequest request, Long userId);

    List<Project> getProjectsByUserId(Long userId);

    int saveUploadedFiles(Long projectId, MultipartFile[] files);
}
