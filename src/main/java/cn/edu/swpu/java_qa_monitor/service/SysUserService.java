package cn.edu.swpu.java_qa_monitor.service;

import cn.edu.swpu.java_qa_monitor.common.dto.LoginRequest;
import cn.edu.swpu.java_qa_monitor.common.dto.LoginResponse;
import cn.edu.swpu.java_qa_monitor.common.dto.RegisterRequest;
import cn.edu.swpu.java_qa_monitor.domain.SysUser;
import com.baomidou.mybatisplus.extension.service.IService;

public interface SysUserService extends IService<SysUser> {

    LoginResponse login(LoginRequest request);

    void register(RegisterRequest request);
}
