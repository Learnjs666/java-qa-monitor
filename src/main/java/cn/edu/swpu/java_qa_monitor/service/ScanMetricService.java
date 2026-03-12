package cn.edu.swpu.java_qa_monitor.service;

import cn.edu.swpu.java_qa_monitor.domain.ScanMetric;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

public interface ScanMetricService extends IService<ScanMetric> {

    List<ScanMetric> getByTaskId(Long taskId);
}
