package cn.edu.swpu.java_qa_monitor.service;

import cn.edu.swpu.java_qa_monitor.common.dto.LoginRequest;
import cn.edu.swpu.java_qa_monitor.common.dto.LoginResponse;
import cn.edu.swpu.java_qa_monitor.common.dto.RegisterRequest;
import cn.edu.swpu.java_qa_monitor.domain.SysUser;
import com.baomidou.mybatisplus.extension.service.IService;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

// test
public interface SysUserService extends IService<SysUser> {

    LoginResponse login(LoginRequest request);

    void register(RegisterRequest request);

    void updateUserRole(Integer id, @NotBlank(message = "角色级别不能为空") @Pattern(regexp = "^(ADMIN|USER)$", message = "非法的权限级别") String role);
}
