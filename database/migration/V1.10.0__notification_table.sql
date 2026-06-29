-- ============================================================================
-- 搭把手数据库迁移脚本 V1.10.0
-- 描述: 系统通知表
-- ============================================================================

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
    FOREIGN KEY (`user_id`) REFERENCES `dbs_user`(`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统通知表';

-- ============================================================================
-- 回滚脚本
-- ============================================================================
-- DROP TABLE IF EXISTS `dbs_notification`;
