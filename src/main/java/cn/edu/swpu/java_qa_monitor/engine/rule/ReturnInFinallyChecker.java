package cn.edu.swpu.java_qa_monitor.engine.rule;

import cn.edu.swpu.java_qa_monitor.domain.RuleConfig;
import cn.edu.swpu.java_qa_monitor.domain.ScanIssue;
import cn.edu.swpu.java_qa_monitor.engine.model.FileAnalysisContext;
import com.github.javaparser.ast.stmt.ReturnStmt;
import com.github.javaparser.ast.stmt.TryStmt;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * SonarQube: squid:S1143
 * finally 块中绝对不能包含 return 语句，它会吞掉在 try/catch 中抛出的所有异常！这是一个严重的逻辑缺陷。
 */
@Component
public class ReturnInFinallyChecker implements RuleChecker {

    @Override
    public String getRuleCode() {
        return "RETURN_IN_FINALLY";
    }

    @Override
    public List<ScanIssue> check(FileAnalysisContext context, RuleConfig config) {
        List<ScanIssue> issues = new ArrayList<>();

        context.getCompilationUnit().findAll(TryStmt.class).forEach(tryStmt -> {
            tryStmt.getFinallyBlock().ifPresent(finallyBlock -> {
                finallyBlock.findAll(ReturnStmt.class).forEach(returnStmt -> {
                    ScanIssue issue = new ScanIssue();
                    issue.setFileName(context.getFileName());
                    issue.setLineNumber(returnStmt.getBegin().map(p -> p.line).orElse(0));
                    issue.setRuleCode(getRuleCode());
                    issue.setSeverity(config.getSeverity());
                    issue.setMessage("禁止在 finally 块中使用 return，这会导致异常被意外吞咽并覆盖正确返回值");
                    issues.add(issue);
                });
            });
        });

        return issues;
    }
}