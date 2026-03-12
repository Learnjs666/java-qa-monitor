package cn.edu.swpu.java_qa_monitor.common.dto;

import lombok.Data;

@Data
public class OverviewResponse {
    private long totalProjects;
    private long totalScans;
    private long totalIssues;
    private long errorCount;
    private long warningCount;
    private long infoCount;
}
