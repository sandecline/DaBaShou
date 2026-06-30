-- ============================================================================
-- 搭把手数据库迁移脚本 V1.5.0
-- 描述: 新增积分账户表、冻结记录表、担保池表
-- 作者: dabashou-dev
-- 日期: 2026-06-29
-- 依赖: V1.0.0 (dbs_user, dbs_order)
-- 变更记录:
--   V1.5.0 (2026-06-29) - 创建积分核心三表，支撑冻结/解冻/结算/担保池逻辑
-- ============================================================================

-- 积分账户表
CREATE TABLE IF NOT EXISTS `dbs_point_account` (
    `id`         BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '账户ID',
    `user_id`    BIGINT   NOT NULL COMMENT '用户ID',
    `available`  INT      NOT NULL DEFAULT 0 COMMENT '可用积分',
    `frozen`     INT      NOT NULL DEFAULT 0 COMMENT '冻结积分',
    `total_earned` INT    NOT NULL DEFAULT 0 COMMENT '累计收入积分',
    `total_spent` INT     NOT NULL DEFAULT 0 COMMENT '累计支出积分',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY `dbs_point_account_uk_user_id` (`user_id`),
    FOREIGN KEY (`user_id`) REFERENCES `dbs_user`(`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='积分账户表';

-- 积分冻结记录表
CREATE TABLE IF NOT EXISTS `dbs_point_freeze` (
    `id`          BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '冻结ID',
    `order_id`    BIGINT   NOT NULL COMMENT '关联订单ID',
    `user_id`     BIGINT   NOT NULL COMMENT '用户ID',
    `amount`      INT      NOT NULL COMMENT '冻结金额',
    `status`      TINYINT  NOT NULL DEFAULT 1 COMMENT '状态: 1-冻结中 2-已解冻 3-已结算',
    `freeze_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '冻结时间',
    `release_time` DATETIME COMMENT '解冻/结算时间',
    UNIQUE KEY `dbs_point_freeze_uk_order_id` (`order_id`),
    INDEX `dbs_point_freeze_idx_user_status` (`user_id`, `status`),
    FOREIGN KEY (`order_id`) REFERENCES `dbs_order`(`id`),
    FOREIGN KEY (`user_id`) REFERENCES `dbs_user`(`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='积分冻结记录表';

-- 担保池表
CREATE TABLE IF NOT EXISTS `dbs_guarantee_pool` (
    `id`          BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '担保ID',
    `order_id`    BIGINT   NOT NULL COMMENT '关联订单ID',
    `amount`      INT      NOT NULL COMMENT '担保金额',
    `status`      TINYINT  NOT NULL DEFAULT 1 COMMENT '状态: 1-担保中 2-已结算给卖家 3-已退还买家',
    `settle_time` DATETIME COMMENT '结算时间',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    UNIQUE KEY `dbs_guarantee_pool_uk_order_id` (`order_id`),
    FOREIGN KEY (`order_id`) REFERENCES `dbs_order`(`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='担保池表';

-- ========== ROLLBACK ==========
-- 回滚步骤（按依赖顺序）:
-- DROP TABLE IF EXISTS `dbs_guarantee_pool`;
-- DROP TABLE IF EXISTS `dbs_point_freeze`;
-- DROP TABLE IF EXISTS `dbs_point_account`;
