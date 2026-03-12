package cn.edu.swpu.java_qa_monitor.engine.rule;

import cn.edu.swpu.java_qa_monitor.domain.RuleConfig;
import cn.edu.swpu.java_qa_monitor.domain.ScanIssue;
import cn.edu.swpu.java_qa_monitor.engine.model.FileAnalysisContext;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class ClassLengthChecker implements RuleChecker {

    @Override
    public String getRuleCode() {
        return "CLASS_TOO_LONG";
    }

    @Override
    public List<ScanIssue> check(FileAnalysisContext context, RuleConfig config) {
        List<ScanIssue> issues = new ArrayList<>();
        int threshold = config.getThresholdValue() != null ? config.getThresholdValue() : 500;

        context.getCompilationUnit().findAll(ClassOrInterfaceDeclaration.class).forEach(cls -> {
            int beginLine = cls.getBegin().map(p -> p.line).orElse(0);
            int endLine = cls.getEnd().map(p -> p.line).orElse(0);
            int classLength = endLine - beginLine + 1;
            if (classLength > threshold) {
                ScanIssue issue = new ScanIssue();
                issue.setFileName(context.getFileName());
                issue.setLineNumber(beginLine);
                issue.setRuleCode(getRuleCode());
                issue.setSeverity(config.getSeverity());
                issue.setMessage("类 " + cls.getNameAsString() + " 过长（" + classLength + " 行，阈值 " + threshold + "）");
                issues.add(issue);
            }
        });

        return issues;
    }
}
