package cn.edu.swpu.java_qa_monitor.engine.rule;

import cn.edu.swpu.java_qa_monitor.domain.RuleConfig;
import cn.edu.swpu.java_qa_monitor.domain.ScanIssue;
import cn.edu.swpu.java_qa_monitor.engine.model.FileAnalysisContext;
import com.github.javaparser.ast.stmt.CatchClause;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * SonarQube: squid:S1181 / squid:S2221
 * 不应直接捕捉 Throwable、Exception 或 Error 等顶级/泛型异常，应当捕获具体的异常以进行正确的处理。
 */
@Component
public class CatchGenericExceptionChecker implements RuleChecker {

    @Override
    public String getRuleCode() {
        return "CATCH_GENERIC_EXCEPTION";
    }

    @Override
    public List<ScanIssue> check(FileAnalysisContext context, RuleConfig config) {
        List<ScanIssue> issues = new ArrayList<>();

        context.getCompilationUnit().findAll(CatchClause.class).forEach(cc -> {
            String caughtType = cc.getParameter().getTypeAsString();

            if ("Throwable".equals(caughtType) || "Exception".equals(caughtType) || "Error".equals(caughtType) ||
                    "java.lang.Throwable".equals(caughtType) || "java.lang.Exception".equals(caughtType)) {

                ScanIssue issue = new ScanIssue();
                issue.setFileName(context.getFileName());
                issue.setLineNumber(cc.getBegin().map(p -> p.line).orElse(0));
                issue.setRuleCode(getRuleCode());
                issue.setSeverity(config.getSeverity());
                issue.setMessage("不要直接捕获 " + caughtType + "，应当精准捕获对应的具体异常类");
                issues.add(issue);
            }
        });

        return issues;
    }
}