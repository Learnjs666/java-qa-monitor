package cn.edu.swpu.java_qa_monitor.engine.rule;

import cn.edu.swpu.java_qa_monitor.domain.RuleConfig;
import cn.edu.swpu.java_qa_monitor.domain.ScanIssue;
import cn.edu.swpu.java_qa_monitor.engine.model.FileAnalysisContext;
import com.github.javaparser.ast.expr.MethodCallExpr;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * SonarQube: squid:S106
 * 标准输出和标准错误不应当用于生产环境代码，应使用 Logger。
 */
@Component
public class SystemOutChecker implements RuleChecker {

    @Override
    public String getRuleCode() {
        return "USE_LOGGER_NOT_SYSTEM_OUT";
    }

    @Override
    public List<ScanIssue> check(FileAnalysisContext context, RuleConfig config) {
        List<ScanIssue> issues = new ArrayList<>();

        context.getCompilationUnit().findAll(MethodCallExpr.class).forEach(mc -> {
            mc.getScope().ifPresent(scope -> {
                String scopeName = scope.toString();
                if ("System.out".equals(scopeName) || "System.err".equals(scopeName)) {
                    String methodName = mc.getNameAsString();
                    if ("print".equals(methodName) || "println".equals(methodName) || "printf".equals(methodName)) {
                        ScanIssue issue = new ScanIssue();
                        issue.setFileName(context.getFileName());
                        issue.setLineNumber(mc.getBegin().map(p -> p.line).orElse(0));
                        issue.setRuleCode(getRuleCode());
                        issue.setSeverity(config.getSeverity());
                        issue.setMessage("禁止在生产代码中使用 " + scopeName + "." + methodName + "()，请使用标准日志记录器(Logger)");
                        issues.add(issue);
                    }
                }
            });
        });

        return issues;
    }
}