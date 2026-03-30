package cn.edu.swpu.java_qa_monitor.common.dto;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

@Data
public class UserRoleUpdateDTO {

    @NotBlank(message = "角色级别不能为空")
    // 可以加个正则校验，防止被恶意传入非法角色
    @Pattern(regexp = "^(ADMIN|USER)$", message = "非法的权限级别")
    private String role;
}
