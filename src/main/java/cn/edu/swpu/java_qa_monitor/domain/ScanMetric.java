package cn.edu.swpu.java_qa_monitor.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

@TableName("scan_metric")
@Data
public class ScanMetric {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long taskId;
    private Long projectId;
    private String fileName;
    private Integer methodCount;
    private Integer lineCount;
    private Integer maxCyclomaticComplexity;
    private Date createTime;
}
