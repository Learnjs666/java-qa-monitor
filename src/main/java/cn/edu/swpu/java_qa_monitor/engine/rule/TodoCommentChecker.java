package cn.edu.swpu.java_qa_monitor.engine.rule;

import cn.edu.swpu.java_qa_monitor.domain.RuleConfig;
import cn.edu.swpu.java_qa_monitor.domain.ScanIssue;
import cn.edu.swpu.java_qa_monitor.engine.model.FileAnalysisContext;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * SonarQube: squid:S1135
 * 代码中包含 TODO 或 FIXME 标记说明业务逻辑未完成，它在正式版本中是必须要被消除的潜在技术债。
 */
@Component
public class TodoCommentChecker implements RuleChecker {

    @Override
    public String getRuleCode() {
        return "TODO_COMMENT_FOUND";
    }

    @Override
    public List<ScanIssue> check(FileAnalysisContext context, RuleConfig config) {
        List<ScanIssue> issues = new ArrayList<>();

        context.getCompilationUnit().getAllComments().forEach(comment -> {
            String content = comment.getContent().toLowerCase();
            if (content.contains("todo") || content.contains("fixme")) {
                ScanIssue issue = new ScanIssue();
                issue.setFileName(context.getFileName());
                issue.setLineNumber(comment.getBegin().map(p -> p.line).orElse(0));
                issue.setRuleCode(getRuleCode());
                issue.setSeverity(config.getSeverity());
                issue.setMessage("发现代码中残留 TODO 或 FIXME 标记标签，说明存在未结清的技术债务");
                issues.add(issue);
            }
        });

        return issues;
    }
}