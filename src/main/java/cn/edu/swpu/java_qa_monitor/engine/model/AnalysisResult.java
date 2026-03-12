package cn.edu.swpu.java_qa_monitor.engine.model;

import cn.edu.swpu.java_qa_monitor.domain.ScanIssue;
import cn.edu.swpu.java_qa_monitor.domain.ScanMetric;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class AnalysisResult {
    private List<ScanIssue> issues = new ArrayList<>();
    private ScanMetric metric;

    public void addIssue(ScanIssue issue) {
        issues.add(issue);
    }
}
