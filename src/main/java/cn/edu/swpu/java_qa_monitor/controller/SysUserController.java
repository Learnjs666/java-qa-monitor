package cn.edu.swpu.java_qa_monitor.controller;

import cn.edu.swpu.java_qa_monitor.common.context.UserContext;
import cn.edu.swpu.java_qa_monitor.common.dto.LoginRequest;
import cn.edu.swpu.java_qa_monitor.common.dto.LoginResponse;
import cn.edu.swpu.java_qa_monitor.common.dto.RegisterRequest;
import cn.edu.swpu.java_qa_monitor.common.result.Result;
import cn.edu.swpu.java_qa_monitor.domain.SysUser;
import cn.edu.swpu.java_qa_monitor.service.SysUserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class

    SysUserController {

    private final SysUserService sysUserService;

    @PostMapping("/login")
    public Result<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = sysUserService.login(request);
        return Result.success(response);
    }

    @PostMapping("/register")
    public Result<Void> register(@Valid @RequestBody RegisterRequest request) {
        sysUserService.register(request);
        return Result.success(null);
    }

    @GetMapping("/info")
    public Result<Map<String, Object>> getUserInfo() {
        UserContext.UserInfo userInfo = UserContext.get();
        return Result.success(Map.of(
                "userId", userInfo.getUserId(),
                "username", userInfo.getUsername(),
                "role", userInfo.getRole()
        ));
    }

    @GetMapping("/list")
    public Result<List<SysUser>> getUserList() {
        List<SysUser> list = sysUserService.list();
        return Result.success(list);
    }

}
