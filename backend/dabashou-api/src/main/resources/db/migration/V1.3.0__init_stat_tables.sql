-- ============================================================================
-- 搭把手数据库迁移脚本 V1.3.0
-- 描述: 初始化数据统计表（2张）
-- 作者: dabashou-dev
-- 日期: 2026-06-28
-- 变更记录:
--   V1.3.0 (2026-06-28) - 初始版本，创建数据统计表
-- 依赖: V1.0.0 (dbs_skill_tag, dbs_skill_category)
-- ============================================================================

-- 每日汇总统计表
CREATE TABLE IF NOT EXISTS `stat_daily_summary` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '统计ID',
    `stat_date` DATE NOT NULL UNIQUE COMMENT '统计日期',
    `new_user_count` INT DEFAULT 0 COMMENT '新增用户数',
    `active_user_count` INT DEFAULT 0 COMMENT '活跃用户数',
    `new_demand_count` INT DEFAULT 0 COMMENT '新增需求数',
    `new_order_count` INT DEFAULT 0 COMMENT '新增订单数',
    `completed_order_count` INT DEFAULT 0 COMMENT '完成订单数',
    `cancelled_order_count` INT DEFAULT 0 COMMENT '取消订单数',
    `point_inflow` BIGINT DEFAULT 0 COMMENT '积分流入量',
    `point_outflow` BIGINT DEFAULT 0 COMMENT '积分流出量',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='每日汇总统计表';

-- 技能热度统计表
CREATE TABLE IF NOT EXISTS `stat_skill_heat` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '统计ID',
    `stat_date` DATE NOT NULL COMMENT '统计日期',
    `skill_tag_id` BIGINT NOT NULL COMMENT '技能标签ID',
    `category_id` BIGINT NOT NULL COMMENT '分类ID',
    `shelf_count` INT DEFAULT 0 COMMENT '上架服务数',
    `demand_count` INT DEFAULT 0 COMMENT '需求数',
    `order_count` INT DEFAULT 0 COMMENT '订单数',
    `heat_score` DECIMAL(10,2) DEFAULT 0 COMMENT '热度分',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY `uk_date_skill` (`stat_date`, `skill_tag_id`),
    INDEX `idx_category` (`category_id`),
    INDEX `idx_heat_score` (`heat_score`),
    FOREIGN KEY (`skill_tag_id`) REFERENCES `dbs_skill_tag`(`id`),
    FOREIGN KEY (`category_id`) REFERENCES `dbs_skill_category`(`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='技能热度统计表';

-- ============================================================================
-- ROLLBACK (逆序删除，尊重外键约束)
-- ============================================================================
-- DROP TABLE IF EXISTS `stat_skill_heat`;
-- DROP TABLE IF EXISTS `stat_daily_summary`;
