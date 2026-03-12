package cn.edu.swpu.java_qa_monitor.engine.rule;

import cn.edu.swpu.java_qa_monitor.domain.RuleConfig;
import cn.edu.swpu.java_qa_monitor.domain.ScanIssue;
import cn.edu.swpu.java_qa_monitor.engine.model.FileAnalysisContext;
import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

@Component
public class NamingConventionChecker implements RuleChecker {

    private static final Pattern UPPER_CAMEL = Pattern.compile("^[A-Z][a-zA-Z0-9]*$");
    private static final Pattern LOWER_CAMEL = Pattern.compile("^[a-z][a-zA-Z0-9]*$");
    private static final Pattern UPPER_SNAKE = Pattern.compile("^[A-Z][A-Z0-9_]*$");

    @Override
    public String getRuleCode() {
        return "CLASS_NAME_CONVENTION";
    }

    @Override
    public List<ScanIssue> check(FileAnalysisContext context, RuleConfig config) {
        List<ScanIssue> issues = new ArrayList<>();

        // Check class names
        context.getCompilationUnit().findAll(ClassOrInterfaceDeclaration.class).forEach(cls -> {
            String name = cls.getNameAsString();
            if (!UPPER_CAMEL.matcher(name).matches()) {
                ScanIssue issue = new ScanIssue();
                issue.setFileName(context.getFileName());
                issue.setLineNumber(cls.getBegin().map(p -> p.line).orElse(0));
                issue.setRuleCode("CLASS_NAME_CONVENTION");
                issue.setSeverity(config.getSeverity());
                issue.setMessage("类名 " + name + " 不符合UpperCamelCase规范");
                issues.add(issue);
            }
        });

        // Check method names
        context.getCompilationUnit().findAll(MethodDeclaration.class).forEach(md -> {
            String name = md.getNameAsString();
            if (!LOWER_CAMEL.matcher(name).matches()) {
                ScanIssue issue = new ScanIssue();
                issue.setFileName(context.getFileName());
                issue.setLineNumber(md.getBegin().map(p -> p.line).orElse(0));
                issue.setRuleCode("METHOD_NAME_CONVENTION");
                issue.setSeverity(config.getSeverity());
                issue.setMessage("方法名 " + name + " 不符合lowerCamelCase规范");
                issues.add(issue);
            }
        });

        // Check field names
        context.getCompilationUnit().findAll(FieldDeclaration.class).forEach(fd -> {
            boolean isStatic = fd.getModifiers().stream()
                    .anyMatch(m -> m.getKeyword() == Modifier.Keyword.STATIC);
            boolean isFinal = fd.getModifiers().stream()
                    .anyMatch(m -> m.getKeyword() == Modifier.Keyword.FINAL);
            boolean isConstant = isStatic && isFinal;

            for (VariableDeclarator vd : fd.getVariables()) {
                String name = vd.getNameAsString();
                if (isConstant) {
                    if (!UPPER_SNAKE.matcher(name).matches()) {
                        ScanIssue issue = new ScanIssue();
                        issue.setFileName(context.getFileName());
                        issue.setLineNumber(fd.getBegin().map(p -> p.line).orElse(0));
                        issue.setRuleCode("FIELD_NAME_CONVENTION");
                        issue.setSeverity(config.getSeverity());
                        issue.setMessage("常量名 " + name + " 不符合UPPER_SNAKE_CASE规范");
                        issues.add(issue);
                    }
                } else {
                    if (!LOWER_CAMEL.matcher(name).matches()) {
                        ScanIssue issue = new ScanIssue();
                        issue.setFileName(context.getFileName());
                        issue.setLineNumber(fd.getBegin().map(p -> p.line).orElse(0));
                        issue.setRuleCode("FIELD_NAME_CONVENTION");
                        issue.setSeverity(config.getSeverity());
                        issue.setMessage("字段名 " + name + " 不符合lowerCamelCase规范");
                        issues.add(issue);
                    }
                }
            }
        });

        return issues;
    }
}
