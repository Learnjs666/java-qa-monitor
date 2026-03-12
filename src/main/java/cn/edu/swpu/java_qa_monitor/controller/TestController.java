package cn.edu.swpu.java_qa_monitor.controller;

import cn.edu.swpu.java_qa_monitor.common.result.Result;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {

    @GetMapping("/test/success")
    public Result<String> testSuccess() {
        return Result.success("后端基建联通成功！");
    }

    @GetMapping("/test/error")
    public Result<String> testError() {
        // 故意制造一个除零异常，测试全局异常处理器是否生效
        int a = 1 / 0;
        return Result.success("这行代码永远不会执行");
    }
}