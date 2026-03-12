package cn.edu.swpu.java_qa_monitor.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

@TableName("scan_task")
@Data
public class ScanTask {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long projectId;
    private Long userId;
    private String status;
    private Integer issueCount;
    private Integer fileCount;
    private String errorMessage;
    private Date createTime;
    private Date updateTime;
}
