package cn.edu.swpu.java_qa_monitor.service.impl;

import cn.edu.swpu.java_qa_monitor.domain.ScanIssue;
import cn.edu.swpu.java_qa_monitor.mapper.ScanIssueMapper;
import cn.edu.swpu.java_qa_monitor.service.ScanIssueService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ScanIssueServiceImpl extends ServiceImpl<ScanIssueMapper, ScanIssue>
        implements ScanIssueService {

    @Override
    public List<ScanIssue> getByTaskId(Long taskId) {
        return list(new LambdaQueryWrapper<ScanIssue>()
                .eq(ScanIssue::getTaskId, taskId)
                .orderByAsc(ScanIssue::getFileName)
                .orderByAsc(ScanIssue::getLineNumber));
    }
}




