-- ============================================================================
-- 搭把手数据库迁移脚本 V1.21.0
-- 描述: 订单表添加争议相关字段（争议原因、补充说明、发起人、发起时间）
-- 作者: dabashou-dev
-- 日期: 2026-07-05
-- ============================================================================

-- 1) 争议原因
ALTER TABLE `dbs_order`
    ADD COLUMN `dispute_reason` VARCHAR(500) DEFAULT NULL COMMENT '争议原因' AFTER `cancel_reason`;

-- 2) 争议补充说明
ALTER TABLE `dbs_order`
    ADD COLUMN `dispute_explain` VARCHAR(500) DEFAULT NULL COMMENT '争议补充说明' AFTER `dispute_reason`;

-- 3) 争议发起人ID
ALTER TABLE `dbs_order`
    ADD COLUMN `dispute_user_id` BIGINT DEFAULT NULL COMMENT '争议发起人用户ID' AFTER `dispute_explain`;

-- 4) 争议发起时间
ALTER TABLE `dbs_order`
    ADD COLUMN `dispute_time` DATETIME DEFAULT NULL COMMENT '争议发起时间' AFTER `dispute_user_id`;

-- 5) 争议发起人索引
ALTER TABLE `dbs_order`
    ADD INDEX `idx_dispute_user` (`dispute_user_id`);
