package cn.edu.swpu.java_qa_monitor.engine;

import cn.edu.swpu.java_qa_monitor.domain.*;
import cn.edu.swpu.java_qa_monitor.engine.model.AnalysisResult;
import cn.edu.swpu.java_qa_monitor.engine.model.FileAnalysisContext;
import cn.edu.swpu.java_qa_monitor.engine.rule.CyclomaticComplexityChecker;
import cn.edu.swpu.java_qa_monitor.engine.rule.RuleChecker;
import cn.edu.swpu.java_qa_monitor.service.RuleConfigService;
import cn.edu.swpu.java_qa_monitor.service.ScanIssueService;
import cn.edu.swpu.java_qa_monitor.service.ScanMetricService;
import cn.edu.swpu.java_qa_monitor.service.ScanTaskService;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.MethodDeclaration;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Service
public class CodeAnalysisService {

    private final List<RuleChecker> ruleCheckers;
    private final RuleConfigService ruleConfigService;
    private final ScanIssueService scanIssueService;
    private final ScanMetricService scanMetricService;
    private final ScanTaskService scanTaskService;

    public CodeAnalysisService(List<RuleChecker> ruleCheckers,
                               RuleConfigService ruleConfigService,
                               ScanIssueService scanIssueService,
                               ScanMetricService scanMetricService,
                               @Lazy ScanTaskService scanTaskService) {
        this.ruleCheckers = ruleCheckers;
        this.ruleConfigService = ruleConfigService;
        this.scanIssueService = scanIssueService;
        this.scanMetricService = scanMetricService;
        this.scanTaskService = scanTaskService;
    }

    @Async
    public void runAnalysis(Long taskId, Project project) {
        ScanTask task = scanTaskService.getById(taskId);
        try {
            task.setStatus("RUNNING");
            scanTaskService.updateById(task);

            List<RuleConfig> enabledRules = ruleConfigService.getEnabledRules();
            Map<String, RuleConfig> ruleMap = enabledRules.stream()
                    .collect(Collectors.toMap(RuleConfig::getRuleCode, r -> r));

            Path sourcePath = Paths.get(project.getSourcePath());
            List<Path> javaFiles;
            try (Stream<Path> walk = Files.walk(sourcePath)) {
                javaFiles = walk
                        .filter(p -> p.toString().endsWith(".java"))
                        .collect(Collectors.toList());
            }

            List<ScanIssue> allIssues = new ArrayList<>();
            List<ScanMetric> allMetrics = new ArrayList<>();

            for (Path javaFile : javaFiles) {
                try {
                    AnalysisResult result = analyzeFile(javaFile, sourcePath, taskId, project.getId(), ruleMap);
                    allIssues.addAll(result.getIssues());
                    if (result.getMetric() != null) {
                        allMetrics.add(result.getMetric());
                    }
                } catch (Exception e) {
                    log.warn("解析文件失败: {}, 原因: {}", javaFile, e.getMessage());
                }
            }

            // Batch save
            if (!allIssues.isEmpty()) {
                scanIssueService.saveBatch(allIssues);
            }
            if (!allMetrics.isEmpty()) {
                scanMetricService.saveBatch(allMetrics);
            }

            task.setStatus("COMPLETED");
            task.setIssueCount(allIssues.size());
            task.setFileCount(javaFiles.size());
            scanTaskService.updateById(task);

        } catch (Exception e) {
            log.error("扫描任务执行失败: taskId={}", taskId, e);
            task.setStatus("FAILED");
            task.setErrorMessage(e.getMessage());
            scanTaskService.updateById(task);
        }
    }

    private AnalysisResult analyzeFile(Path javaFile, Path basePath, Long taskId, Long projectId,
                                       Map<String, RuleConfig> ruleMap) throws IOException {
        AnalysisResult result = new AnalysisResult();
        String relativeName = basePath.relativize(javaFile).toString().replace("\\", "/");
        String source = Files.readString(javaFile);
        int totalLines = source.split("\n").length;

        CompilationUnit cu = StaticJavaParser.parse(source);
        FileAnalysisContext context = new FileAnalysisContext(relativeName, cu, totalLines);

        // Run all enabled rule checkers
        for (RuleChecker checker : ruleCheckers) {
            RuleConfig config = ruleMap.get(checker.getRuleCode());
            if (config == null) continue;

            List<ScanIssue> issues = checker.check(context, config);
            for (ScanIssue issue : issues) {
                // Check if there's a matching rule config for the actual ruleCode in the issue
                RuleConfig issueConfig = ruleMap.get(issue.getRuleCode());
                if (issueConfig != null) {
                    issue.setTaskId(taskId);
                    issue.setProjectId(projectId);
                    result.addIssue(issue);
                }
            }
        }

        // Collect metrics
        ScanMetric metric = new ScanMetric();
        metric.setTaskId(taskId);
        metric.setProjectId(projectId);
        metric.setFileName(relativeName);
        metric.setLineCount(totalLines);

        List<MethodDeclaration> methods = cu.findAll(MethodDeclaration.class);
        metric.setMethodCount(methods.size());

        int maxComplexity = 0;
        CyclomaticComplexityChecker ccChecker = ruleCheckers.stream()
                .filter(c -> c instanceof CyclomaticComplexityChecker)
                .map(c -> (CyclomaticComplexityChecker) c)
                .findFirst().orElse(null);

        if (ccChecker != null) {
            for (MethodDeclaration md : methods) {
                int complexity = ccChecker.calculateComplexity(md);
                maxComplexity = Math.max(maxComplexity, complexity);
            }
        }
        metric.setMaxCyclomaticComplexity(maxComplexity);
        result.setMetric(metric);

        return result;
    }
}
