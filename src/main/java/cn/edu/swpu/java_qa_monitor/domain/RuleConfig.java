package cn.edu.swpu.java_qa_monitor.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

@TableName("rule_config")
@Data
public class RuleConfig {

    @TableId(type = IdType.AUTO)
    private Long id;
    private String ruleCode;
    private String ruleName;
    private String category;
    private String severity;
    private Integer enabled;
    private Integer thresholdValue;
    private String description;
    private Date createTime;
    private Date updateTime;
}
