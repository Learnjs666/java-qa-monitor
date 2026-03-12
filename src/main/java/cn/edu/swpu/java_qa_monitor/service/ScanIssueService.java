package cn.edu.swpu.java_qa_monitor.service;

import cn.edu.swpu.java_qa_monitor.domain.ScanIssue;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
* @author 86188
* @description 针对表【scan_issue(缂洪櫡鏄庣粏琛)】的数据库操作Service
* @createDate 2026-03-12 04:28:37
*/
public interface ScanIssueService extends IService<ScanIssue> {

    List<ScanIssue> getByTaskId(Long taskId);
}
