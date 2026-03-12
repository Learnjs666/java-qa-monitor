package cn.edu.swpu.java_qa_monitor.mapper;

import cn.edu.swpu.java_qa_monitor.domain.SysUser;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
* @author 86188
* @description 针对表【sys_user(系统用户表)】的数据库操作Mapper
* @createDate 2026-03-12 01:04:30
* @Entity cn.edu.swpu.java_qa_monitor.domain.SysUser
*/
@Mapper
public interface SysUserMapper extends BaseMapper<SysUser> {

}




