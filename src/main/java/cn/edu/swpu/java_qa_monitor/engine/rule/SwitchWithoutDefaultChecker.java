package cn.edu.swpu.java_qa_monitor.engine.rule;

import cn.edu.swpu.java_qa_monitor.domain.RuleConfig;
import cn.edu.swpu.java_qa_monitor.domain.ScanIssue;
import cn.edu.swpu.java_qa_monitor.engine.model.FileAnalysisContext;
import com.github.javaparser.ast.stmt.SwitchStmt;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * SonarQube: squid:S131
 * Switch 语句必须包含 default 分支，防止遗漏逻辑处理。
 */
@Component
public class SwitchWithoutDefaultChecker implements RuleChecker {

    @Override
    public String getRuleCode() {
        return "SWITCH_WITHOUT_DEFAULT";
    }

    @Override
    public List<ScanIssue> check(FileAnalysisContext context, RuleConfig config) {
        List<ScanIssue> issues = new ArrayList<>();

        context.getCompilationUnit().findAll(SwitchStmt.class).forEach(switchStmt -> {
            // 在 JavaParser 中，Label 列表为空的 SwitchEntry 就是 default 分支
            boolean hasDefault = switchStmt.getEntries().stream()
                    .anyMatch(entry -> entry.getLabels().isEmpty());

            if (!hasDefault) {
                ScanIssue issue = new ScanIssue();
                issue.setFileName(context.getFileName());
                issue.setLineNumber(switchStmt.getBegin().map(p -> p.line).orElse(0));
                issue.setRuleCode(getRuleCode());
                issue.setSeverity(config.getSeverity());
                issue.setMessage("switch 语句应当包含 default 分支，以处理所有未知情况");
                issues.add(issue);
            }
        });

        return issues;
    }
}