package cn.edu.swpu.java_qa_monitor.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.util.Date;
import lombok.Data;

/**
 * 缂洪櫡鏄庣粏琛
 * @TableName scan_issue
 */
@TableName(value ="scan_issue")
@Data
public class ScanIssue {
    /**
     * 涓婚敭ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 浠诲姟ID
     */
    private Long taskId;

    /**
     * 椤圭洰ID
     */
    private Long projectId;

    /**
     * 鏂囦欢鍚
     */
    private String fileName;

    /**
     * 琛屽彿
     */
    private Integer lineNumber;

    /**
     * 瑙勫垯缂栫爜
     */
    private String ruleCode;

    /**
     * 涓ラ噸绾у埆: ERROR/WARNING/INFO
     */
    private String severity;

    /**
     * 缂洪櫡鎻忚堪
     */
    private String message;

    /**
     * 鍒涘缓鏃堕棿
     */
    private Date createTime;

    @Override
    public boolean equals(Object that) {
        if (this == that) {
            return true;
        }
        if (that == null) {
            return false;
        }
        if (getClass() != that.getClass()) {
            return false;
        }
        ScanIssue other = (ScanIssue) that;
        return (this.getId() == null ? other.getId() == null : this.getId().equals(other.getId()))
            && (this.getTaskId() == null ? other.getTaskId() == null : this.getTaskId().equals(other.getTaskId()))
            && (this.getProjectId() == null ? other.getProjectId() == null : this.getProjectId().equals(other.getProjectId()))
            && (this.getFileName() == null ? other.getFileName() == null : this.getFileName().equals(other.getFileName()))
            && (this.getLineNumber() == null ? other.getLineNumber() == null : this.getLineNumber().equals(other.getLineNumber()))
            && (this.getRuleCode() == null ? other.getRuleCode() == null : this.getRuleCode().equals(other.getRuleCode()))
            && (this.getSeverity() == null ? other.getSeverity() == null : this.getSeverity().equals(other.getSeverity()))
            && (this.getMessage() == null ? other.getMessage() == null : this.getMessage().equals(other.getMessage()))
            && (this.getCreateTime() == null ? other.getCreateTime() == null : this.getCreateTime().equals(other.getCreateTime()));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getId() == null) ? 0 : getId().hashCode());
        result = prime * result + ((getTaskId() == null) ? 0 : getTaskId().hashCode());
        result = prime * result + ((getProjectId() == null) ? 0 : getProjectId().hashCode());
        result = prime * result + ((getFileName() == null) ? 0 : getFileName().hashCode());
        result = prime * result + ((getLineNumber() == null) ? 0 : getLineNumber().hashCode());
        result = prime * result + ((getRuleCode() == null) ? 0 : getRuleCode().hashCode());
        result = prime * result + ((getSeverity() == null) ? 0 : getSeverity().hashCode());
        result = prime * result + ((getMessage() == null) ? 0 : getMessage().hashCode());
        result = prime * result + ((getCreateTime() == null) ? 0 : getCreateTime().hashCode());
        return result;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        sb.append(" [");
        sb.append("Hash = ").append(hashCode());
        sb.append(", id=").append(id);
        sb.append(", taskId=").append(taskId);
        sb.append(", projectId=").append(projectId);
        sb.append(", fileName=").append(fileName);
        sb.append(", lineNumber=").append(lineNumber);
        sb.append(", ruleCode=").append(ruleCode);
        sb.append(", severity=").append(severity);
        sb.append(", message=").append(message);
        sb.append(", createTime=").append(createTime);
        sb.append("]");
        return sb.toString();
    }
}