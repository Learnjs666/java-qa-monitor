package cn.edu.swpu.java_qa_monitor.engine.model;

import com.github.javaparser.ast.CompilationUnit;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class FileAnalysisContext {
    private String fileName;
    private CompilationUnit compilationUnit;
    private int totalLines;
}
