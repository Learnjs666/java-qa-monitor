package cn.edu.swpu.java_qa_monitor.service.impl;

import cn.edu.swpu.java_qa_monitor.common.exception.BusinessException;
import cn.edu.swpu.java_qa_monitor.domain.RuleConfig;
import cn.edu.swpu.java_qa_monitor.mapper.RuleConfigMapper;
import cn.edu.swpu.java_qa_monitor.service.RuleConfigService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RuleConfigServiceImpl extends ServiceImpl<RuleConfigMapper, RuleConfig>
        implements RuleConfigService {

    @Override
    public List<RuleConfig> getEnabledRules() {
        return list(new LambdaQueryWrapper<RuleConfig>()
                .eq(RuleConfig::getEnabled, 1));
    }

    @Override
    public void updateRule(Long id, RuleConfig ruleConfig) {
        RuleConfig existing = getById(id);
        if (existing == null) {
            throw new BusinessException("规则不存在");
        }
        if (ruleConfig.getSeverity() != null) {
            existing.setSeverity(ruleConfig.getSeverity());
        }
        if (ruleConfig.getEnabled() != null) {
            existing.setEnabled(ruleConfig.getEnabled());
        }
        if (ruleConfig.getThresholdValue() != null) {
            existing.setThresholdValue(ruleConfig.getThresholdValue());
        }
        updateById(existing);
    }
}
