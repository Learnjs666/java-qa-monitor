package cn.edu.swpu.java_qa_monitor.engine.rule;

import cn.edu.swpu.java_qa_monitor.domain.RuleConfig;
import cn.edu.swpu.java_qa_monitor.domain.ScanIssue;
import cn.edu.swpu.java_qa_monitor.engine.model.FileAnalysisContext;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.stmt.ReturnStmt;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * SonarQube: squid:S1142
 * 方法中包含过多的 return 语句会使控制流变得难以理解，违反了结构化编程原则。
 */
@Component
public class MultipleReturnChecker implements RuleChecker {

    @Override
    public String getRuleCode() {
        return "TOO_MANY_RETURNS";
    }

    @Override
    public List<ScanIssue> check(FileAnalysisContext context, RuleConfig config) {
        List<ScanIssue> issues = new ArrayList<>();
        // 默认若超过 3 个 return 即视为坏味道
        int threshold = config.getThresholdValue() != null ? config.getThresholdValue() : 3;

        context.getCompilationUnit().findAll(MethodDeclaration.class).forEach(md -> {
            int returnCount = md.findAll(ReturnStmt.class).size();

            if (returnCount > threshold) {
                ScanIssue issue = new ScanIssue();
                issue.setFileName(context.getFileName());
                issue.setLineNumber(md.getBegin().map(p -> p.line).orElse(0));
                issue.setRuleCode(getRuleCode());
                issue.setSeverity(config.getSeverity());
                issue.setMessage("方法 " + md.getNameAsString() + " 中使用了 " + returnCount + " 个 return 语句（阈值 " + threshold + "），影响可读性");
                issues.add(issue);
            }
        });

        return issues;
    }
}