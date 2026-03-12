package cn.edu.swpu.java_qa_monitor.common.dto;

import lombok.Data;

@Data
public class RuleConfigUpdateRequest {
    private String severity;
    private Integer enabled;
    private Integer thresholdValue;
}
