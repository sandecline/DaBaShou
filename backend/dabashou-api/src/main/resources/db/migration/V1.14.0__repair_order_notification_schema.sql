-- ============================================================================
-- 搭把手数据库迁移脚本 V1.14.0
-- 描述: 修复本地环境可能缺失的订单、通知、积分和用户扩展表结构
-- 日期: 2026-07-01
-- 依赖: V1.0.0, V1.2.0, V1.5.0, V1.10.0
-- ============================================================================

DELIMITER $$

DROP PROCEDURE IF EXISTS repair_order_notification_schema $$
CREATE PROCEDURE repair_order_notification_schema()
BEGIN
    DECLARE missing_count INT DEFAULT 0;

    SELECT COUNT(*) INTO missing_count
        FROM information_schema.`COLUMNS`
        WHERE `TABLE_SCHEMA` = DATABASE()
          AND `TABLE_NAME` = 'dbs_order'
          AND `COLUMN_NAME` = 'deleted';
    IF missing_count = 0 THEN
        ALTER TABLE `dbs_order`
            ADD COLUMN `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除: 0-未删除 1-已删除' AFTER `update_time`;
    END IF;

    SELECT COUNT(*) INTO missing_count
        FROM information_schema.`COLUMNS`
        WHERE `TABLE_SCHEMA` = DATABASE()
          AND `TABLE_NAME` = 'dbs_order'
          AND `COLUMN_NAME` = 'remark';
    IF missing_count = 0 THEN
        ALTER TABLE `dbs_order`
            ADD COLUMN `remark` VARCHAR(500) NULL COMMENT '备注' AFTER `cancel_reason`;
    END IF;

    SELECT COUNT(*) INTO missing_count
        FROM information_schema.`STATISTICS`
        WHERE `TABLE_SCHEMA` = DATABASE()
          AND `TABLE_NAME` = 'dbs_order'
          AND `INDEX_NAME` = 'idx_buyer_status';
    IF missing_count = 0 THEN
        ALTER TABLE `dbs_order`
            ADD INDEX `idx_buyer_status` (`buyer_id`, `status`);
    END IF;

    SELECT COUNT(*) INTO missing_count
        FROM information_schema.`STATISTICS`
        WHERE `TABLE_SCHEMA` = DATABASE()
          AND `TABLE_NAME` = 'dbs_order'
          AND `INDEX_NAME` = 'idx_seller_status';
    IF missing_count = 0 THEN
        ALTER TABLE `dbs_order`
            ADD INDEX `idx_seller_status` (`seller_id`, `status`);
    END IF;

    SELECT COUNT(*) INTO missing_count
        FROM information_schema.`STATISTICS`
        WHERE `TABLE_SCHEMA` = DATABASE()
          AND `TABLE_NAME` = 'dbs_order'
          AND `INDEX_NAME` = 'uk_verify_code';
    IF missing_count = 0 THEN
        ALTER TABLE `dbs_order`
            ADD UNIQUE KEY `uk_verify_code` (`verify_code`);
    END IF;

    CREATE TABLE IF NOT EXISTS `dbs_notification` (
        `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '通知ID',
        `user_id` BIGINT NOT NULL COMMENT '接收用户ID',
        `type` VARCHAR(30) NOT NULL COMMENT '通知类型: system/order/credit/point/chat',
        `title` VARCHAR(100) NOT NULL COMMENT '通知标题',
        `content` VARCHAR(500) COMMENT '通知内容',
        `related_type` VARCHAR(30) COMMENT '关联类型: order/review/violation',
        `related_id` BIGINT COMMENT '关联业务ID',
        `is_read` TINYINT DEFAULT 0 COMMENT '是否已读: 0-未读 1-已读',
        `read_time` DATETIME COMMENT '已读时间',
        `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
        INDEX `idx_user_read` (`user_id`, `is_read`),
        INDEX `idx_user_time` (`user_id`, `create_time`),
        CONSTRAINT `fk_notification_user` FOREIGN KEY (`user_id`) REFERENCES `dbs_user`(`id`)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统通知表';

    CREATE TABLE IF NOT EXISTS `dbs_point_account` (
        `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '积分账户ID',
        `user_id` BIGINT NOT NULL COMMENT '用户ID',
        `available` INT NOT NULL DEFAULT 0 COMMENT '可用积分',
        `frozen` INT NOT NULL DEFAULT 0 COMMENT '冻结积分',
        `total_earned` INT NOT NULL DEFAULT 0 COMMENT '累计收入积分',
        `total_spent` INT NOT NULL DEFAULT 0 COMMENT '累计支出积分',
        `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
        `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
        UNIQUE KEY `uk_user_id` (`user_id`),
        CONSTRAINT `fk_point_account_user` FOREIGN KEY (`user_id`) REFERENCES `dbs_user`(`id`)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='积分账户表';

    INSERT IGNORE INTO `dbs_point_account` (`user_id`, `available`, `frozen`, `total_earned`, `total_spent`)
        SELECT `id`, COALESCE(`point_balance`, 0), 0, COALESCE(`point_balance`, 0), 0
        FROM `dbs_user`;

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
        INDEX `idx_user` (`user_id`),
        INDEX `idx_order` (`order_id`),
        INDEX `idx_type` (`type`),
        CONSTRAINT `fk_trust_score_log_user` FOREIGN KEY (`user_id`) REFERENCES `dbs_user`(`id`),
        CONSTRAINT `fk_trust_score_log_order` FOREIGN KEY (`order_id`) REFERENCES `dbs_order`(`id`)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户信任分变动记录表';

    INSERT IGNORE INTO `dbs_user_trust_score_log`
        (`id`, `user_id`, `order_id`, `type`, `score_change`, `score_before`, `score_after`, `reason`, `create_time`)
        SELECT `id`, `user_id`, `order_id`, `type`, `score_change`, `score_before`, `score_after`, `reason`, `create_time`
        FROM `user_trust_score_log`;

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
        UNIQUE KEY `uk_user_id` (`user_id`),
        CONSTRAINT `fk_campus_auth_user` FOREIGN KEY (`user_id`) REFERENCES `dbs_user`(`id`),
        CONSTRAINT `fk_campus_auth_file` FOREIGN KEY (`credential_file_id`) REFERENCES `sys_file`(`id`)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户校园认证表';

    INSERT IGNORE INTO `dbs_user_campus_auth`
        (`id`, `user_id`, `auth_type`, `student_no`, `real_name`, `campus`, `college`, `id_card_hash`, `credential_file_id`, `status`, `review_remark`, `reviewer_id`, `review_time`, `create_time`, `update_time`)
        SELECT `id`, `user_id`, `auth_type`, `student_no`, `real_name`, `campus`, `college`, `id_card_hash`, `credential_file_id`, `status`, `review_remark`, `reviewer_id`, `review_time`, `create_time`, `update_time`
        FROM `user_campus_auth`;
END $$

CALL repair_order_notification_schema() $$
DROP PROCEDURE IF EXISTS repair_order_notification_schema $$

DELIMITER ;

-- ============================================================================
-- 回滚脚本
-- ============================================================================
-- DROP TABLE IF EXISTS `dbs_notification`;
-- DROP TABLE IF EXISTS `dbs_user_campus_auth`;
-- DROP TABLE IF EXISTS `dbs_user_trust_score_log`;
-- DROP TABLE IF EXISTS `dbs_point_account`;
-- ALTER TABLE `dbs_order` DROP INDEX `uk_verify_code`;
-- ALTER TABLE `dbs_order` DROP INDEX `idx_seller_status`;
-- ALTER TABLE `dbs_order` DROP INDEX `idx_buyer_status`;
-- ALTER TABLE `dbs_order` DROP COLUMN `remark`;
-- ALTER TABLE `dbs_order` DROP COLUMN `deleted`;
