-- ============================================================================
-- 搭把手数据库迁移脚本 V1.4.0
-- 描述: 新增聊天表 + 需求类型字段
-- 作者: dabashou-dev
-- 日期: 2026-06-28
-- 变更记录:
--   V1.4.0 (2026-06-28) - 新增聊天会话/消息表，需求表新增类型字段
-- 依赖: V1.0.0 (dbs_user, dbs_demand)
-- ============================================================================

-- 需求表新增类型字段
ALTER TABLE `dbs_demand` ADD COLUMN `demand_type` TINYINT NOT NULL DEFAULT 1 COMMENT '需求类型: 1-求助悬赏 2-技能置换' AFTER `status`;

-- 聊天会话表
CREATE TABLE IF NOT EXISTS `dbs_chat_session` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '会话ID',
    `user1_id` BIGINT NOT NULL COMMENT '用户1ID',
    `user2_id` BIGINT NOT NULL COMMENT '用户2ID',
    `last_message` VARCHAR(500) COMMENT '最后一条消息',
    `last_time` DATETIME COMMENT '最后消息时间',
    `unread_count` INT DEFAULT 0 COMMENT '未读消息数',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX `idx_user1` (`user1_id`),
    INDEX `idx_user2` (`user2_id`),
    FOREIGN KEY (`user1_id`) REFERENCES `dbs_user`(`id`),
    FOREIGN KEY (`user2_id`) REFERENCES `dbs_user`(`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='聊天会话表';

-- 聊天消息表
CREATE TABLE IF NOT EXISTS `dbs_chat_message` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '消息ID',
    `session_id` BIGINT NOT NULL COMMENT '会话ID',
    `sender_id` BIGINT NOT NULL COMMENT '发送者ID',
    `content` TEXT NOT NULL COMMENT '消息内容',
    `msg_type` TINYINT DEFAULT 1 COMMENT '消息类型: 1-文字 2-图片',
    `is_read` TINYINT DEFAULT 0 COMMENT '是否已读: 0-未读 1-已读',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX `idx_session` (`session_id`),
    INDEX `idx_sender` (`sender_id`),
    INDEX `idx_create_time` (`create_time`),
    FOREIGN KEY (`session_id`) REFERENCES `dbs_chat_session`(`id`),
    FOREIGN KEY (`sender_id`) REFERENCES `dbs_user`(`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='聊天消息表';
