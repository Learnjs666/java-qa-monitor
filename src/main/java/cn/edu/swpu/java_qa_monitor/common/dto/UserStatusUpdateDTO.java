package cn.edu.swpu.java_qa_monitor.common.dto;

import lombok.Data;
import jakarta.validation.constraints.NotNull;

@Data
public class UserStatusUpdateDTO {
    @NotNull(message = "状态参数不能为空")
    private Boolean status;
}
