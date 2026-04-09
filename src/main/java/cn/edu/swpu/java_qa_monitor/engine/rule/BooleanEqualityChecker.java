package cn.edu.swpu.java_qa_monitor.engine.rule;

import cn.edu.swpu.java_qa_monitor.domain.RuleConfig;
import cn.edu.swpu.java_qa_monitor.domain.ScanIssue;
import cn.edu.swpu.java_qa_monitor.engine.model.FileAnalysisContext;
import com.github.javaparser.ast.expr.BinaryExpr;
import com.github.javaparser.ast.expr.BooleanLiteralExpr;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * SonarQube: squid:S1125
 * 不应该将 boolean 变量或表达式显式地与 true 或 false 进行 == 或 != 判断（例如 `if (b == true)`）。
 */
@Component
public class BooleanEqualityChecker implements RuleChecker {

    @Override
    public String getRuleCode() {
        return "REDUNDANT_BOOLEAN_EQUALITY";
    }

    @Override
    public List<ScanIssue> check(FileAnalysisContext context, RuleConfig config) {
        List<ScanIssue> issues = new ArrayList<>();

        context.getCompilationUnit().findAll(BinaryExpr.class).forEach(be -> {
            if (be.getOperator() == BinaryExpr.Operator.EQUALS || be.getOperator() == BinaryExpr.Operator.NOT_EQUALS) {
                // 如果表达式的左侧或者右侧出现了直接的布尔值常量
                if (be.getLeft() instanceof BooleanLiteralExpr || be.getRight() instanceof BooleanLiteralExpr) {
                    ScanIssue issue = new ScanIssue();
                    issue.setFileName(context.getFileName());
                    issue.setLineNumber(be.getBegin().map(p -> p.line).orElse(0));
                    issue.setRuleCode(getRuleCode());
                    issue.setSeverity(config.getSeverity());
                    issue.setMessage("多余的布尔判断：布尔表达式不应该和 true/false 字面量直接用 == 或 != 比较");
                    issues.add(issue);
                }
            }
        });

        return issues;
    }
}