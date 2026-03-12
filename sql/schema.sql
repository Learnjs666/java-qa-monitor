-- Java QA Monitor 数据库 DDL
-- 在已有 sys_user 表的基础上，新增以下 5 张表

-- 1. 代码项目表
CREATE TABLE IF NOT EXISTS `project` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `user_id` BIGINT NOT NULL COMMENT '所属用户ID',
    `name` VARCHAR(100) NOT NULL COMMENT '项目名称',
    `description` VARCHAR(500) DEFAULT '' COMMENT '项目描述',
    `source_path` VARCHAR(500) NOT NULL COMMENT '源码存储路径',
    `file_count` INT DEFAULT 0 COMMENT 'Java文件数量',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `is_deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除(0-正常, 1-删除)',
    PRIMARY KEY (`id`),
    INDEX `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='代码项目表';

-- 2. 扫描任务表
CREATE TABLE IF NOT EXISTS `scan_task` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `project_id` BIGINT NOT NULL COMMENT '项目ID',
    `user_id` BIGINT NOT NULL COMMENT '触发用户ID',
    `status` VARCHAR(20) NOT NULL DEFAULT 'PENDING' COMMENT '状态: PENDING/RUNNING/COMPLETED/FAILED',
    `issue_count` INT DEFAULT 0 COMMENT '缺陷总数',
    `file_count` INT DEFAULT 0 COMMENT '扫描文件数',
    `error_message` VARCHAR(1000) DEFAULT NULL COMMENT '错误信息',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    INDEX `idx_project_id` (`project_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='扫描任务表';

-- 3. 缺陷明细表
CREATE TABLE IF NOT EXISTS `scan_issue` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `task_id` BIGINT NOT NULL COMMENT '任务ID',
    `project_id` BIGINT NOT NULL COMMENT '项目ID',
    `file_name` VARCHAR(300) NOT NULL COMMENT '文件名',
    `line_number` INT DEFAULT 0 COMMENT '行号',
    `rule_code` VARCHAR(50) NOT NULL COMMENT '规则编码',
    `severity` VARCHAR(20) NOT NULL COMMENT '严重级别: ERROR/WARNING/INFO',
    `message` VARCHAR(1000) NOT NULL COMMENT '缺陷描述',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    INDEX `idx_task_id` (`task_id`),
    INDEX `idx_project_id` (`project_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='缺陷明细表';

-- 4. 质量指标表
CREATE TABLE IF NOT EXISTS `scan_metric` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `task_id` BIGINT NOT NULL COMMENT '任务ID',
    `project_id` BIGINT NOT NULL COMMENT '项目ID',
    `file_name` VARCHAR(300) NOT NULL COMMENT '文件名',
    `method_count` INT DEFAULT 0 COMMENT '方法数量',
    `line_count` INT DEFAULT 0 COMMENT '代码行数',
    `max_cyclomatic_complexity` INT DEFAULT 0 COMMENT '最大圈复杂度',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    INDEX `idx_task_id` (`task_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='质量指标表';

-- 5. 规则配置表
CREATE TABLE IF NOT EXISTS `rule_config` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `rule_code` VARCHAR(50) NOT NULL COMMENT '规则编码',
    `rule_name` VARCHAR(100) NOT NULL COMMENT '规则名称',
    `category` VARCHAR(50) NOT NULL COMMENT '分类: NAMING/COMPLEXITY/STYLE/DOC/BEST_PRACTICE',
    `severity` VARCHAR(20) NOT NULL DEFAULT 'WARNING' COMMENT '严重级别: ERROR/WARNING/INFO',
    `enabled` TINYINT NOT NULL DEFAULT 1 COMMENT '是否启用(0-禁用, 1-启用)',
    `threshold_value` INT DEFAULT NULL COMMENT '阈值',
    `description` VARCHAR(500) DEFAULT '' COMMENT '规则描述',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE INDEX `uk_rule_code` (`rule_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='规则配置表';

-- 预置 10 条规则
INSERT INTO `rule_config` (`rule_code`, `rule_name`, `category`, `severity`, `enabled`, `threshold_value`, `description`) VALUES
('METHOD_TOO_LONG', '方法过长', 'STYLE', 'WARNING', 1, 50, '方法体超过阈值行数'),
('CLASS_TOO_LONG', '类过长', 'STYLE', 'WARNING', 1, 500, '类文件超过阈值行数'),
('HIGH_CYCLOMATIC', '高圈复杂度', 'COMPLEXITY', 'ERROR', 1, 10, '方法圈复杂度超过阈值'),
('CLASS_NAME_CONVENTION', '类名不规范', 'NAMING', 'WARNING', 1, NULL, '类名应为UpperCamelCase'),
('METHOD_NAME_CONVENTION', '方法名不规范', 'NAMING', 'WARNING', 1, NULL, '方法名应为lowerCamelCase'),
('FIELD_NAME_CONVENTION', '字段名不规范', 'NAMING', 'WARNING', 1, NULL, '字段名应为lowerCamelCase，常量应为UPPER_SNAKE_CASE'),
('MISSING_JAVADOC', '缺少Javadoc', 'DOC', 'INFO', 1, NULL, '公共方法缺少Javadoc注释'),
('PARAM_COUNT_EXCEEDED', '参数过多', 'STYLE', 'WARNING', 1, 5, '方法参数数量超过阈值'),
('EMPTY_CATCH_BLOCK', '空catch块', 'BEST_PRACTICE', 'ERROR', 1, NULL, 'catch块不应为空'),
('MAGIC_NUMBER', '魔法数字', 'BEST_PRACTICE', 'INFO', 1, NULL, '代码中不应直接使用魔法数字');
