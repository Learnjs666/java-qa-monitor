package cn.edu.swpu.java_qa_monitor.service.impl;

import cn.edu.swpu.java_qa_monitor.domain.ScanMetric;
import cn.edu.swpu.java_qa_monitor.mapper.ScanMetricMapper;
import cn.edu.swpu.java_qa_monitor.service.ScanMetricService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ScanMetricServiceImpl extends ServiceImpl<ScanMetricMapper, ScanMetric>
        implements ScanMetricService {

    @Override
    public List<ScanMetric> getByTaskId(Long taskId) {
        return list(new LambdaQueryWrapper<ScanMetric>()
                .eq(ScanMetric::getTaskId, taskId));
    }
}
