package cn.edu.swpu.java_qa_monitor.engine.rule;

import cn.edu.swpu.java_qa_monitor.domain.RuleConfig;
import cn.edu.swpu.java_qa_monitor.domain.ScanIssue;
import cn.edu.swpu.java_qa_monitor.engine.model.FileAnalysisContext;
import com.github.javaparser.ast.stmt.CatchClause;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class EmptyCatchChecker implements RuleChecker {

    @Override
    public String getRuleCode() {
        return "EMPTY_CATCH_BLOCK";
    }

    @Override
    public List<ScanIssue> check(FileAnalysisContext context, RuleConfig config) {
        List<ScanIssue> issues = new ArrayList<>();

        context.getCompilationUnit().findAll(CatchClause.class).forEach(cc -> {
            if (cc.getBody().getStatements().isEmpty()) {
                ScanIssue issue = new ScanIssue();
                issue.setFileName(context.getFileName());
                issue.setLineNumber(cc.getBegin().map(p -> p.line).orElse(0));
                issue.setRuleCode(getRuleCode());
                issue.setSeverity(config.getSeverity());
                issue.setMessage("catch块为空，应至少记录日志或处理异常");
                issues.add(issue);
            }
        });

        return issues;
    }
}
