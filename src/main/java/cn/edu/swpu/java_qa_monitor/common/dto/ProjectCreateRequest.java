package cn.edu.swpu.java_qa_monitor.common.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ProjectCreateRequest {

    @NotBlank(message = "项目名称不能为空")
    private String name;

    private String description;
}
