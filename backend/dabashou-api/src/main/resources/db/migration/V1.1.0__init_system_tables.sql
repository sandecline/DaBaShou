-- ============================================================================
-- 搭把手数据库迁移脚本 V1.1.0
-- 描述: 初始化系统管理表（8张）
-- 作者: dabashou-dev
-- 日期: 2026-06-28
-- 变更记录:
--   V1.1.0 (2026-06-28) - 初始版本，创建系统管理表
-- 依赖: V1.0.0 (dbs_user)
-- ============================================================================

-- 角色表
CREATE TABLE IF NOT EXISTS `sys_role` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '角色ID',
    `role_code` VARCHAR(50) NOT NULL UNIQUE COMMENT '角色编码',
    `role_name` VARCHAR(50) NOT NULL COMMENT '角色名称',
    `description` VARCHAR(200) COMMENT '角色描述',
    `status` TINYINT DEFAULT 1 COMMENT '状态: 0-禁用 1-正常',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色表';

-- 权限表
CREATE TABLE IF NOT EXISTS `sys_permission` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '权限ID',
    `parent_id` BIGINT DEFAULT 0 COMMENT '父权限ID',
    `permission_code` VARCHAR(100) NOT NULL UNIQUE COMMENT '权限编码',
    `permission_name` VARCHAR(50) NOT NULL COMMENT '权限名称',
    `type` TINYINT NOT NULL COMMENT '类型: 1-菜单 2-按钮',
    `path` VARCHAR(200) COMMENT '路由路径',
    `component` VARCHAR(200) COMMENT '组件路径',
    `icon` VARCHAR(100) COMMENT '图标',
    `sort_order` INT DEFAULT 0 COMMENT '排序',
    `status` TINYINT DEFAULT 1 COMMENT '状态: 0-禁用 1-正常',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='权限表';

-- 角色-权限关联表
CREATE TABLE IF NOT EXISTS `sys_role_permission` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'ID',
    `role_id` BIGINT NOT NULL COMMENT '角色ID',
    `permission_id` BIGINT NOT NULL COMMENT '权限ID',
    UNIQUE KEY `uk_role_permission` (`role_id`, `permission_id`),
    FOREIGN KEY (`role_id`) REFERENCES `sys_role`(`id`),
    FOREIGN KEY (`permission_id`) REFERENCES `sys_permission`(`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色-权限关联表';

-- 用户-角色关联表
CREATE TABLE IF NOT EXISTS `sys_user_role` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `role_id` BIGINT NOT NULL COMMENT '角色ID',
    UNIQUE KEY `uk_user_role` (`user_id`, `role_id`),
    FOREIGN KEY (`user_id`) REFERENCES `dbs_user`(`id`),
    FOREIGN KEY (`role_id`) REFERENCES `sys_role`(`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户-角色关联表';

-- 文件表
CREATE TABLE IF NOT EXISTS `sys_file` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '文件ID',
    `original_name` VARCHAR(255) NOT NULL COMMENT '原始文件名',
    `file_name` VARCHAR(255) NOT NULL COMMENT '存储文件名',
    `file_path` VARCHAR(500) NOT NULL COMMENT '文件路径',
    `file_url` VARCHAR(500) COMMENT '访问URL',
    `file_size` BIGINT COMMENT '文件大小(字节)',
    `file_type` VARCHAR(50) COMMENT '文件类型',
    `mime_type` VARCHAR(100) COMMENT 'MIME类型',
    `upload_user_id` BIGINT COMMENT '上传用户ID',
    `status` TINYINT DEFAULT 1 COMMENT '状态: 0-删除 1-正常',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX `idx_upload_user` (`upload_user_id`),
    INDEX `idx_file_type` (`file_type`),
    FOREIGN KEY (`upload_user_id`) REFERENCES `dbs_user`(`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='文件表';

-- 通知表
CREATE TABLE IF NOT EXISTS `sys_notification` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '通知ID',
    `user_id` BIGINT NOT NULL COMMENT '接收用户ID',
    `type` VARCHAR(50) NOT NULL COMMENT '通知类型',
    `title` VARCHAR(100) NOT NULL COMMENT '通知标题',
    `content` TEXT COMMENT '通知内容',
    `related_type` VARCHAR(50) COMMENT '关联业务类型',
    `related_id` BIGINT COMMENT '关联业务ID',
    `is_read` TINYINT DEFAULT 0 COMMENT '是否已读: 0-未读 1-已读',
    `read_time` DATETIME COMMENT '已读时间',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX `idx_user` (`user_id`),
    INDEX `idx_is_read` (`is_read`),
    INDEX `idx_type` (`type`),
    FOREIGN KEY (`user_id`) REFERENCES `dbs_user`(`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='通知表';

-- 操作日志表
CREATE TABLE IF NOT EXISTS `sys_log` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '日志ID',
    `operator_id` BIGINT COMMENT '操作人ID',
    `operation_type` VARCHAR(50) NOT NULL COMMENT '操作类型',
    `operation_content` VARCHAR(500) COMMENT '操作内容',
    `method` VARCHAR(200) COMMENT '请求方法',
    `request_url` VARCHAR(500) COMMENT '请求URL',
    `request_params` TEXT COMMENT '请求参数',
    `ip` VARCHAR(50) COMMENT 'IP地址',
    `status` TINYINT COMMENT '操作状态: 0-失败 1-成功',
    `error_msg` TEXT COMMENT '错误信息',
    `cost_time_ms` INT COMMENT '耗时(毫秒)',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX `idx_operator` (`operator_id`),
    INDEX `idx_operation_type` (`operation_type`),
    INDEX `idx_create_time` (`create_time`),
    FOREIGN KEY (`operator_id`) REFERENCES `dbs_user`(`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='操作日志表';

-- 系统配置表
CREATE TABLE IF NOT EXISTS `sys_config` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '配置ID',
    `config_key` VARCHAR(100) NOT NULL UNIQUE COMMENT '配置键',
    `config_value` TEXT COMMENT '配置值',
    `config_name` VARCHAR(100) NOT NULL COMMENT '配置名称',
    `config_type` VARCHAR(50) COMMENT '配置类型',
    `description` VARCHAR(500) COMMENT '配置描述',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统配置表';

-- ============================================================================
-- ROLLBACK (逆序删除，尊重外键约束)
-- ============================================================================
-- DROP TABLE IF EXISTS `sys_config`;
-- DROP TABLE IF EXISTS `sys_log`;
-- DROP TABLE IF EXISTS `sys_notification`;
-- DROP TABLE IF EXISTS `sys_file`;
-- DROP TABLE IF EXISTS `sys_user_role`;
-- DROP TABLE IF EXISTS `sys_role_permission`;
-- DROP TABLE IF EXISTS `sys_permission`;
-- DROP TABLE IF EXISTS `sys_role`;
