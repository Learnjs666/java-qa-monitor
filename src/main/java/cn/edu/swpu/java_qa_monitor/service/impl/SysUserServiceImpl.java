package cn.edu.swpu.java_qa_monitor.service.impl;

import cn.edu.swpu.java_qa_monitor.common.dto.LoginRequest;
import cn.edu.swpu.java_qa_monitor.common.dto.LoginResponse;
import cn.edu.swpu.java_qa_monitor.common.dto.RegisterRequest;
import cn.edu.swpu.java_qa_monitor.common.exception.BusinessException;
import cn.edu.swpu.java_qa_monitor.common.util.JwtUtil;
import cn.edu.swpu.java_qa_monitor.common.util.PasswordUtil;
import cn.edu.swpu.java_qa_monitor.domain.SysUser;
import cn.edu.swpu.java_qa_monitor.mapper.SysUserMapper;
import cn.edu.swpu.java_qa_monitor.service.SysUserService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SysUserServiceImpl extends ServiceImpl<SysUserMapper, SysUser>
        implements SysUserService {

    private final JwtUtil jwtUtil;

    @Override
    public LoginResponse login(LoginRequest request) {
        SysUser user = getOne(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUsername, request.getUsername()));
        if (user == null) {
            throw new BusinessException("用户名或密码错误");
        }
        if (!PasswordUtil.matches(request.getPassword(), user.getPassword())) {
            throw new BusinessException("用户名或密码错误");
        }
        String token = jwtUtil.generateToken(user.getId(), user.getUsername(), user.getRole());
        return new LoginResponse(token, user.getUsername(), user.getRole());
    }

    @Override
    public void register(RegisterRequest request) {
        long count = count(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUsername, request.getUsername()));
        if (count > 0) {
            throw new BusinessException("用户名已存在");
        }
        SysUser user = new SysUser();
        user.setUsername(request.getUsername());
        user.setPassword(PasswordUtil.encode(request.getPassword()));
        user.setRole(request.getRole() != null ? request.getRole() : "USER");
        save(user);
    }
}
