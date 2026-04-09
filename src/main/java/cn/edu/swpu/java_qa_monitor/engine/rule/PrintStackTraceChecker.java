package cn.edu.swpu.java_qa_monitor.engine.rule;

import cn.edu.swpu.java_qa_monitor.domain.RuleConfig;
import cn.edu.swpu.java_qa_monitor.domain.ScanIssue;
import cn.edu.swpu.java_qa_monitor.engine.model.FileAnalysisContext;
import com.github.javaparser.ast.expr.MethodCallExpr;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * SonarQube: squid:S1148
 * 不应调用 Throwable.printStackTrace()，容易导致日志信息丢失，应抛出或记录在 Logger 中。
 */
@Component
public class PrintStackTraceChecker implements RuleChecker {

    @Override
    public String getRuleCode() {
        return "AVOID_PRINT_STACK_TRACE";
    }

    @Override
    public List<ScanIssue> check(FileAnalysisContext context, RuleConfig config) {
        List<ScanIssue> issues = new ArrayList<>();

        context.getCompilationUnit().findAll(MethodCallExpr.class).forEach(mc -> {
            if ("printStackTrace".equals(mc.getNameAsString()) && mc.getArguments().isEmpty()) {
                ScanIssue issue = new ScanIssue();
                issue.setFileName(context.getFileName());
                issue.setLineNumber(mc.getBegin().map(p -> p.line).orElse(0));
                issue.setRuleCode(getRuleCode());
                issue.setSeverity(config.getSeverity());
                issue.setMessage("请勿直接调用 e.printStackTrace()，应当使用日志框架记录异常堆栈");
                issues.add(issue);
            }
        });

        return issues;
    }
}