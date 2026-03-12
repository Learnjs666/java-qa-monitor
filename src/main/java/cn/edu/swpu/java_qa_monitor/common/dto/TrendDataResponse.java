package cn.edu.swpu.java_qa_monitor.common.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
public class TrendDataResponse {

    private List<TrendPoint> points;

    @Data
    @AllArgsConstructor
    public static class TrendPoint {
        private String date;
        private long errorCount;
        private long warningCount;
        private long infoCount;
    }
}
