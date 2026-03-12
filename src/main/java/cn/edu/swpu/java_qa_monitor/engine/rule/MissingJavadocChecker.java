package cn.edu.swpu.java_qa_monitor.engine.rule;

import cn.edu.swpu.java_qa_monitor.domain.RuleConfig;
import cn.edu.swpu.java_qa_monitor.domain.ScanIssue;
import cn.edu.swpu.java_qa_monitor.engine.model.FileAnalysisContext;
import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.body.MethodDeclaration;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class MissingJavadocChecker implements RuleChecker {

    @Override
    public String getRuleCode() {
        return "MISSING_JAVADOC";
    }

    @Override
    public List<ScanIssue> check(FileAnalysisContext context, RuleConfig config) {
        List<ScanIssue> issues = new ArrayList<>();

        context.getCompilationUnit().findAll(MethodDeclaration.class).forEach(md -> {
            boolean isPublic = md.getModifiers().stream()
                    .anyMatch(m -> m.getKeyword() == Modifier.Keyword.PUBLIC);
            if (isPublic && !md.getJavadoc().isPresent()) {
                ScanIssue issue = new ScanIssue();
                issue.setFileName(context.getFileName());
                issue.setLineNumber(md.getBegin().map(p -> p.line).orElse(0));
                issue.setRuleCode(getRuleCode());
                issue.setSeverity(config.getSeverity());
                issue.setMessage("公共方法 " + md.getNameAsString() + " 缺少Javadoc注释");
                issues.add(issue);
            }
        });

        return issues;
    }
}
