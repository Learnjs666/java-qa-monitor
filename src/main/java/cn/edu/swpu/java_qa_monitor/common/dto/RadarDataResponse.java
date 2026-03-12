package cn.edu.swpu.java_qa_monitor.common.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
public class RadarDataResponse {

    private List<DimensionScore> scores;

    @Data
    @AllArgsConstructor
    public static class DimensionScore {
        private String dimension;
        private int score;
    }
}
