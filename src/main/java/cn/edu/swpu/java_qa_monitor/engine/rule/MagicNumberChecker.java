package cn.edu.swpu.java_qa_monitor.engine.rule;

import cn.edu.swpu.java_qa_monitor.domain.RuleConfig;
import cn.edu.swpu.java_qa_monitor.domain.ScanIssue;
import cn.edu.swpu.java_qa_monitor.engine.model.FileAnalysisContext;
import com.github.javaparser.ast.expr.IntegerLiteralExpr;
import com.github.javaparser.ast.expr.DoubleLiteralExpr;
import com.github.javaparser.ast.expr.LongLiteralExpr;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.Modifier;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Component
public class MagicNumberChecker implements RuleChecker {

    private static final Set<String> ALLOWED_VALUES = Set.of("0", "1", "-1", "2", "0.0", "1.0", "0L", "1L");

    @Override
    public String getRuleCode() {
        return "MAGIC_NUMBER";
    }

    @Override
    public List<ScanIssue> check(FileAnalysisContext context, RuleConfig config) {
        List<ScanIssue> issues = new ArrayList<>();

        // Find magic numbers in method bodies (not in field declarations that are constants)
        context.getCompilationUnit().findAll(IntegerLiteralExpr.class).forEach(expr -> {
            if (ALLOWED_VALUES.contains(expr.getValue())) return;
            if (isInConstantField(expr)) return;

            ScanIssue issue = new ScanIssue();
            issue.setFileName(context.getFileName());
            issue.setLineNumber(expr.getBegin().map(p -> p.line).orElse(0));
            issue.setRuleCode(getRuleCode());
            issue.setSeverity(config.getSeverity());
            issue.setMessage("魔法数字 " + expr.getValue() + "，建议提取为常量");
            issues.add(issue);
        });

        context.getCompilationUnit().findAll(DoubleLiteralExpr.class).forEach(expr -> {
            if (ALLOWED_VALUES.contains(expr.getValue())) return;
            if (isInConstantField(expr)) return;

            ScanIssue issue = new ScanIssue();
            issue.setFileName(context.getFileName());
            issue.setLineNumber(expr.getBegin().map(p -> p.line).orElse(0));
            issue.setRuleCode(getRuleCode());
            issue.setSeverity(config.getSeverity());
            issue.setMessage("魔法数字 " + expr.getValue() + "，建议提取为常量");
            issues.add(issue);
        });

        return issues;
    }

    private boolean isInConstantField(com.github.javaparser.ast.Node node) {
        return node.findAncestor(FieldDeclaration.class)
                .filter(fd -> fd.getModifiers().stream()
                        .anyMatch(m -> m.getKeyword() == Modifier.Keyword.STATIC)
                        && fd.getModifiers().stream()
                        .anyMatch(m -> m.getKeyword() == Modifier.Keyword.FINAL))
                .isPresent();
    }
}
