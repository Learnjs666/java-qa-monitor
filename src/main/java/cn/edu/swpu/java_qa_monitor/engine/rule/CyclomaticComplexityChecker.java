package cn.edu.swpu.java_qa_monitor.engine.rule;

import cn.edu.swpu.java_qa_monitor.domain.RuleConfig;
import cn.edu.swpu.java_qa_monitor.domain.ScanIssue;
import cn.edu.swpu.java_qa_monitor.engine.model.FileAnalysisContext;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.BinaryExpr;
import com.github.javaparser.ast.expr.ConditionalExpr;
import com.github.javaparser.ast.stmt.*;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class CyclomaticComplexityChecker implements RuleChecker {

    @Override
    public String getRuleCode() {
        return "HIGH_CYCLOMATIC";
    }

    @Override
    public List<ScanIssue> check(FileAnalysisContext context, RuleConfig config) {
        List<ScanIssue> issues = new ArrayList<>();
        int threshold = config.getThresholdValue() != null ? config.getThresholdValue() : 10;

        context.getCompilationUnit().findAll(MethodDeclaration.class).forEach(md -> {
            int complexity = calculateComplexity(md);
            if (complexity > threshold) {
                ScanIssue issue = new ScanIssue();
                issue.setFileName(context.getFileName());
                issue.setLineNumber(md.getBegin().map(p -> p.line).orElse(0));
                issue.setRuleCode(getRuleCode());
                issue.setSeverity(config.getSeverity());
                issue.setMessage("方法 " + md.getNameAsString() + " 圈复杂度过高（" + complexity + "，阈值 " + threshold + "）");
                issues.add(issue);
            }
        });

        return issues;
    }

    public int calculateComplexity(MethodDeclaration md) {
        AtomicInteger complexity = new AtomicInteger(1); // Base complexity

        md.accept(new VoidVisitorAdapter<Void>() {
            @Override
            public void visit(IfStmt n, Void arg) {
                super.visit(n, arg);
                complexity.incrementAndGet();
            }

            @Override
            public void visit(ForStmt n, Void arg) {
                super.visit(n, arg);
                complexity.incrementAndGet();
            }

            @Override
            public void visit(ForEachStmt n, Void arg) {
                super.visit(n, arg);
                complexity.incrementAndGet();
            }

            @Override
            public void visit(WhileStmt n, Void arg) {
                super.visit(n, arg);
                complexity.incrementAndGet();
            }

            @Override
            public void visit(DoStmt n, Void arg) {
                super.visit(n, arg);
                complexity.incrementAndGet();
            }

            @Override
            public void visit(SwitchEntry n, Void arg) {
                super.visit(n, arg);
                if (!n.getLabels().isEmpty()) {
                    complexity.incrementAndGet();
                }
            }

            @Override
            public void visit(CatchClause n, Void arg) {
                super.visit(n, arg);
                complexity.incrementAndGet();
            }

            @Override
            public void visit(ConditionalExpr n, Void arg) {
                super.visit(n, arg);
                complexity.incrementAndGet();
            }

            @Override
            public void visit(BinaryExpr n, Void arg) {
                super.visit(n, arg);
                if (n.getOperator() == BinaryExpr.Operator.AND ||
                        n.getOperator() == BinaryExpr.Operator.OR) {
                    complexity.incrementAndGet();
                }
            }
        }, null);

        return complexity.get();
    }
}
