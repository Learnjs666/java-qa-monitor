package cn.edu.swpu.java_qa_monitor.service;

import cn.edu.swpu.java_qa_monitor.domain.RuleConfig;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

public interface RuleConfigService extends IService<RuleConfig> {

    List<RuleConfig> getEnabledRules();

    void updateRule(Long id, RuleConfig ruleConfig);
}
