package cn.edu.swpu.java_qa_monitor.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

@TableName("project")
@Data
public class Project {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private String name;
    private String description;
    private String sourcePath;
    private Integer fileCount;
    private Date createTime;
    private Date updateTime;
    private Integer isDeleted;
}
