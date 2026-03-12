package cn.edu.swpu.java_qa_monitor.engine.rule;

import cn.edu.swpu.java_qa_monitor.domain.RuleConfig;
import cn.edu.swpu.java_qa_monitor.domain.ScanIssue;
import cn.edu.swpu.java_qa_monitor.engine.model.FileAnalysisContext;
import com.github.javaparser.ast.body.MethodDeclaration;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class ParamCountChecker implements RuleChecker {

    @Override
    public String getRuleCode() {
        return "PARAM_COUNT_EXCEEDED";
    }

    @Override
    public List<ScanIssue> check(FileAnalysisContext context, RuleConfig config) {
        List<ScanIssue> issues = new ArrayList<>();
        int threshold = config.getThresholdValue() != null ? config.getThresholdValue() : 5;

        context.getCompilationUnit().findAll(MethodDeclaration.class).forEach(md -> {
            int paramCount = md.getParameters().size();
            if (paramCount > threshold) {
                ScanIssue issue = new ScanIssue();
                issue.setFileName(context.getFileName());
                issue.setLineNumber(md.getBegin().map(p -> p.line).orElse(0));
                issue.setRuleCode(getRuleCode());
                issue.setSeverity(config.getSeverity());
                issue.setMessage("方法 " + md.getNameAsString() + " 参数过多（" + paramCount + " 个，阈值 " + threshold + "）");
                issues.add(issue);
            }
        });

        return issues;
    }
}
