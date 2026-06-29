-- ============================================================================
-- 搭把手数据库迁移脚本 V1.2.0
-- 描述: 初始化用户认证与信用表（4张）
-- 作者: dabashou-dev
-- 日期: 2026-06-28
-- 变更记录:
--   V1.2.0 (2026-06-28) - 初始版本，创建用户认证与信用表
-- 依赖: V1.0.0 (dbs_user, dbs_order), V1.1.0 (sys_file)
-- ============================================================================

-- 校园认证表
CREATE TABLE IF NOT EXISTS `user_campus_auth` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '认证ID',
    `user_id` BIGINT NOT NULL UNIQUE COMMENT '用户ID',
    `auth_type` VARCHAR(20) NOT NULL COMMENT '认证类型: student/teacher',
    `student_no` VARCHAR(30) COMMENT '学号/工号',
    `real_name` VARCHAR(50) COMMENT '真实姓名',
    `campus` VARCHAR(50) COMMENT '校区',
    `college` VARCHAR(100) COMMENT '学院',
    `id_card_hash` VARCHAR(64) COMMENT '身份证号哈希',
    `credential_file_id` BIGINT COMMENT '凭证文件ID',
    `status` TINYINT DEFAULT 0 COMMENT '状态: 0-待审核 1-已通过 2-已拒绝',
    `review_remark` VARCHAR(200) COMMENT '审核备注',
    `reviewer_id` BIGINT COMMENT '审核人ID',
    `review_time` DATETIME COMMENT '审核时间',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    FOREIGN KEY (`user_id`) REFERENCES `dbs_user`(`id`),
    FOREIGN KEY (`credential_file_id`) REFERENCES `sys_file`(`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='校园认证表';

-- 信任分变动记录表
CREATE TABLE IF NOT EXISTS `user_trust_score_log` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '记录ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `order_id` BIGINT COMMENT '关联订单ID',
    `type` VARCHAR(30) NOT NULL COMMENT '变动类型: order_complete/order_cancel/violation/review/system_adjust',
    `score_change` DECIMAL(3,1) NOT NULL COMMENT '变动分值',
    `score_before` DECIMAL(3,1) NOT NULL COMMENT '变动前分数',
    `score_after` DECIMAL(3,1) NOT NULL COMMENT '变动后分数',
    `reason` VARCHAR(200) COMMENT '变动原因',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX `idx_user` (`user_id`),
    INDEX `idx_order` (`order_id`),
    INDEX `idx_type` (`type`),
    FOREIGN KEY (`user_id`) REFERENCES `dbs_user`(`id`),
    FOREIGN KEY (`order_id`) REFERENCES `dbs_order`(`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='信任分变动记录表';

-- 违规记录表
CREATE TABLE IF NOT EXISTS `credit_violation` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '违规ID',
    `user_id` BIGINT NOT NULL COMMENT '违规用户ID',
    `order_id` BIGINT COMMENT '关联订单ID',
    `type` VARCHAR(30) NOT NULL COMMENT '违规类型: cheat/spam/fraud/other',
    `description` VARCHAR(500) COMMENT '违规描述',
    `penalty_score` DECIMAL(3,1) COMMENT '扣分',
    `reporter_id` BIGINT COMMENT '举报人ID',
    `status` TINYINT DEFAULT 1 COMMENT '状态: 0-已撤销 1-有效',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX `idx_user` (`user_id`),
    INDEX `idx_order` (`order_id`),
    INDEX `idx_type` (`type`),
    FOREIGN KEY (`user_id`) REFERENCES `dbs_user`(`id`),
    FOREIGN KEY (`order_id`) REFERENCES `dbs_order`(`id`),
    FOREIGN KEY (`reporter_id`) REFERENCES `dbs_user`(`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='违规记录表';

-- 申诉表
CREATE TABLE IF NOT EXISTS `credit_appeal` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '申诉ID',
    `violation_id` BIGINT NOT NULL COMMENT '违规记录ID',
    `appellant_id` BIGINT NOT NULL COMMENT '申诉人ID',
    `reason` VARCHAR(500) NOT NULL COMMENT '申诉理由',
    `evidence_file_id` BIGINT COMMENT '证据文件ID',
    `status` TINYINT DEFAULT 0 COMMENT '状态: 0-待审核 1-已通过 2-已驳回',
    `reviewer_id` BIGINT COMMENT '审核人ID',
    `review_remark` VARCHAR(200) COMMENT '审核备注',
    `review_time` DATETIME COMMENT '审核时间',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX `idx_violation` (`violation_id`),
    INDEX `idx_appellant` (`appellant_id`),
    FOREIGN KEY (`violation_id`) REFERENCES `credit_violation`(`id`),
    FOREIGN KEY (`appellant_id`) REFERENCES `dbs_user`(`id`),
    FOREIGN KEY (`evidence_file_id`) REFERENCES `sys_file`(`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='申诉表';
