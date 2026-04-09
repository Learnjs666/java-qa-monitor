package cn.edu.swpu.java_qa_monitor.engine.rule;

import cn.edu.swpu.java_qa_monitor.domain.RuleConfig;
import cn.edu.swpu.java_qa_monitor.domain.ScanIssue;
import cn.edu.swpu.java_qa_monitor.engine.model.FileAnalysisContext;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.StringLiteralExpr;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * SonarQube: squid:S1132
 * 字符串比较时，字符串字面量应该放在左侧，以防止产生 NullPointerException。
 */
@Component
public class EqualsOnStringLiteralChecker implements RuleChecker {

    @Override
    public String getRuleCode() {
        return "EQUALS_ON_LITERAL";
    }

    @Override
    public List<ScanIssue> check(FileAnalysisContext context, RuleConfig config) {
        List<ScanIssue> issues = new ArrayList<>();

        context.getCompilationUnit().findAll(MethodCallExpr.class).forEach(mc -> {
            String methodName = mc.getNameAsString();
            if (("equals".equals(methodName) || "equalsIgnoreCase".equals(methodName)) && mc.getArguments().size() == 1) {

                boolean argIsLiteral = mc.getArgument(0) instanceof StringLiteralExpr;
                boolean scopeIsLiteral = mc.getScope().isPresent() && mc.getScope().get() instanceof StringLiteralExpr;

                // 如果参数是字面量，但调用者(Scope)不是字面量 -> 引发空指针的坏味道
                if (argIsLiteral && !scopeIsLiteral) {
                    ScanIssue issue = new ScanIssue();
                    issue.setFileName(context.getFileName());
                    issue.setLineNumber(mc.getBegin().map(p -> p.line).orElse(0));
                    issue.setRuleCode(getRuleCode());
                    issue.setSeverity(config.getSeverity());
                    issue.setMessage("字符串比较时，应将字符串字面量（常量）放在左侧以防止 NullPointerException");
                    issues.add(issue);
                }
            }
        });

        return issues;
    }
}