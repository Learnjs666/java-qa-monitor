package cn.edu.swpu.java_qa_monitor.engine.rule;

import cn.edu.swpu.java_qa_monitor.domain.RuleConfig;
import cn.edu.swpu.java_qa_monitor.domain.ScanIssue;
import cn.edu.swpu.java_qa_monitor.engine.model.FileAnalysisContext;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class MethodLengthChecker implements RuleChecker {

    @Override
    public String getRuleCode() {
        return "METHOD_TOO_LONG";
    }

    @Override
    public List<ScanIssue> check(FileAnalysisContext context, RuleConfig config) {
        List<ScanIssue> issues = new ArrayList<>();
        int threshold = config.getThresholdValue() != null ? config.getThresholdValue() : 50;

        context.getCompilationUnit().accept(new VoidVisitorAdapter<Void>() {
            @Override
            public void visit(MethodDeclaration md, Void arg) {
                super.visit(md, arg);
                md.getBody().ifPresent(body -> {
                    int beginLine = body.getBegin().map(p -> p.line).orElse(0);
                    int endLine = body.getEnd().map(p -> p.line).orElse(0);
                    int methodLength = endLine - beginLine + 1;
                    if (methodLength > threshold) {
                        ScanIssue issue = new ScanIssue();
                        issue.setFileName(context.getFileName());
                        issue.setLineNumber(md.getBegin().map(p -> p.line).orElse(0));
                        issue.setRuleCode(getRuleCode());
                        issue.setSeverity(config.getSeverity());
                        issue.setMessage("方法 " + md.getNameAsString() + " 过长（" + methodLength + " 行，阈值 " + threshold + "）");
                        issues.add(issue);
                    }
                });
            }
        }, null);

        // Also check CLASS_TOO_LONG
        return issues;
    }
}
