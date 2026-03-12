package cn.edu.swpu.java_qa_monitor.common.dto;

import lombok.Data;

import java.util.Date;

@Data
public class ScanTaskResponse {
    private Long id;
    private Long projectId;
    private String projectName;
    private String status;
    private Integer issueCount;
    private Integer fileCount;
    private String errorMessage;
    private Date createTime;
}
