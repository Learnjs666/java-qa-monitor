package cn.edu.swpu.java_qa_monitor.config;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;

@Configuration // 告诉 Spring 这是一个专门的配置类，启动时必须优先加载
// 请务必仔细核对下面这个包路径！差一个字母都会导致找不到 Bean
@MapperScan("cn.edu.swpu.java_qa_monitor.mapper")
public class MybatisPlusConfig {
    // 未来如果我们要加分页插件（PaginationInnerInterceptor），也会写在这个类里
}