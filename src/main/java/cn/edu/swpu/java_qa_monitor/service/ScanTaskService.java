package cn.edu.swpu.java_qa_monitor.service;

import cn.edu.swpu.java_qa_monitor.common.dto.ScanTaskResponse;
import cn.edu.swpu.java_qa_monitor.domain.ScanTask;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

public interface ScanTaskService extends IService<ScanTask> {

    ScanTask createAndRunScan(Long projectId, Long userId);

    List<ScanTaskResponse> getTasksByProjectId(Long projectId);
}
