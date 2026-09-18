-- ============================================================================
-- 搭把手 H2 测试迁移脚本 V1.14.0
-- 描述: 修复订单、通知、积分和用户扩展表结构（H2 简化版）
-- ============================================================================

ALTER TABLE `dbs_order`
    ADD COLUMN `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除: 0-未删除 1-已删除';

ALTER TABLE `dbs_order`
    ADD COLUMN `remark` VARCHAR(500) NULL COMMENT '备注';

CREATE INDEX `idx_buyer_status` ON `dbs_order` (`buyer_id`, `status`);
CREATE INDEX `idx_seller_status` ON `dbs_order` (`seller_id`, `status`);

CREATE TABLE IF NOT EXISTS `dbs_point_account` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '积分账户ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `available` INT NOT NULL DEFAULT 0 COMMENT '可用积分',
    `frozen` INT NOT NULL DEFAULT 0 COMMENT '冻结积分',
    `total_earned` INT NOT NULL DEFAULT 0 COMMENT '累计收入积分',
    `total_spent` INT NOT NULL DEFAULT 0 COMMENT '累计支出积分',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY `uk_point_account_user_id` (`user_id`),
    CONSTRAINT `fk_point_account_user` FOREIGN KEY (`user_id`) REFERENCES `dbs_user`(`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='积分账户表';

CREATE TABLE IF NOT EXISTS `dbs_user_trust_score_log` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '记录ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `order_id` BIGINT COMMENT '关联订单ID',
    `type` VARCHAR(30) NOT NULL COMMENT '变动类型',
    `score_change` DECIMAL(3,1) NOT NULL COMMENT '变动分值',
    `score_before` DECIMAL(3,1) NOT NULL COMMENT '变动前分值',
    `score_after` DECIMAL(3,1) NOT NULL COMMENT '变动后分值',
    `reason` VARCHAR(200) COMMENT '变动原因',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX `idx_user_trust_score_log_user` (`user_id`),
    INDEX `idx_user_trust_score_log_order` (`order_id`),
    INDEX `idx_user_trust_score_log_type` (`type`),
    CONSTRAINT `fk_trust_score_log_user` FOREIGN KEY (`user_id`) REFERENCES `dbs_user`(`id`),
    CONSTRAINT `fk_trust_score_log_order` FOREIGN KEY (`order_id`) REFERENCES `dbs_order`(`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户信任分变动记录表';

CREATE TABLE IF NOT EXISTS `dbs_user_campus_auth` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '认证ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `auth_type` VARCHAR(20) NOT NULL COMMENT '认证类型: student/teacher',
    `student_no` VARCHAR(30) COMMENT '学号或工号',
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
    UNIQUE KEY `uk_campus_auth_user_id` (`user_id`),
    CONSTRAINT `fk_campus_auth_user` FOREIGN KEY (`user_id`) REFERENCES `dbs_user`(`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户校园认证表';
