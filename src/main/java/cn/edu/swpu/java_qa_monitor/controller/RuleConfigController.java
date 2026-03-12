package cn.edu.swpu.java_qa_monitor.controller;

import cn.edu.swpu.java_qa_monitor.common.dto.RuleConfigUpdateRequest;
import cn.edu.swpu.java_qa_monitor.common.result.Result;
import cn.edu.swpu.java_qa_monitor.domain.RuleConfig;
import cn.edu.swpu.java_qa_monitor.service.RuleConfigService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/rule")
@RequiredArgsConstructor
public class RuleConfigController {

    private final RuleConfigService ruleConfigService;

    @GetMapping("/list")
    public Result<List<RuleConfig>> getRuleList() {
        List<RuleConfig> list = ruleConfigService.list();
        return Result.success(list);
    }

    @PutMapping("/{id}")
    public Result<Void> updateRule(@PathVariable Long id, @RequestBody RuleConfigUpdateRequest request) {
        RuleConfig config = new RuleConfig();
        config.setSeverity(request.getSeverity());
        config.setEnabled(request.getEnabled());
        config.setThresholdValue(request.getThresholdValue());
        ruleConfigService.updateRule(id, config);
        return Result.success(null);
    }
}
