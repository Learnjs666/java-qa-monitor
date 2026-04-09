package cn.edu.swpu.java_qa_monitor.engine.rule;

import cn.edu.swpu.java_qa_monitor.domain.RuleConfig;
import cn.edu.swpu.java_qa_monitor.domain.ScanIssue;
import cn.edu.swpu.java_qa_monitor.engine.model.FileAnalysisContext;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.CatchClause;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * SonarQube: squid:S108
 * 代码中不应该出现空的嵌套块(Block)。空 catch 块单独由 EmptyCatchChecker 处理。
 */
@Component
public class EmptyBlockChecker implements RuleChecker {

    @Override
    public String getRuleCode() {
        return "EMPTY_BLOCK";
    }

    @Override
    public List<ScanIssue> check(FileAnalysisContext context, RuleConfig config) {
        List<ScanIssue> issues = new ArrayList<>();

        context.getCompilationUnit().findAll(BlockStmt.class).forEach(block -> {
            if (block.getStatements().isEmpty() && block.getOrphanComments().isEmpty()) {
                // 如果外层是 CatchClause，跳过（留给专业的 EmptyCatchChecker 处理）
                boolean isCatch = block.getParentNode().isPresent() && block.getParentNode().get() instanceof CatchClause;
                if (!isCatch) {
                    ScanIssue issue = new ScanIssue();
                    issue.setFileName(context.getFileName());
                    issue.setLineNumber(block.getBegin().map(p -> p.line).orElse(0));
                    issue.setRuleCode(getRuleCode());
                    issue.setSeverity(config.getSeverity());
                    issue.setMessage("检测到无任何代码和注释的空语句块({})，若是有意为之请至少添加一句注释");
                    issues.add(issue);
                }
            }
        });

        return issues;
    }
}