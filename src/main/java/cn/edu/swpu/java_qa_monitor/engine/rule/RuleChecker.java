package cn.edu.swpu.java_qa_monitor.engine.rule;

import cn.edu.swpu.java_qa_monitor.domain.RuleConfig;
import cn.edu.swpu.java_qa_monitor.domain.ScanIssue;
import cn.edu.swpu.java_qa_monitor.engine.model.FileAnalysisContext;

import java.util.List;

public interface RuleChecker {

    String getRuleCode();

    List<ScanIssue> check(FileAnalysisContext context, RuleConfig config);
}
