-- ============================================================================
-- 搭把手数据库迁移脚本 V1.20.0
-- 描述: 申诉表支持订单申诉（无需违规记录）
-- 作者: dabashou-dev
-- 日期: 2026-07-04
-- ============================================================================

-- 1) 增加 order_id 列，存放订单申诉关联的订单ID
ALTER TABLE `credit_appeal`
    ADD COLUMN `order_id` BIGINT COMMENT '关联订单ID（订单申诉时使用）' AFTER `violation_id`;

-- 2) violation_id 改为可空（订单申诉时无需违规记录）
ALTER TABLE `credit_appeal`
    MODIFY COLUMN `violation_id` BIGINT NULL COMMENT '违规记录ID（订单申诉时可为空）';

-- 3) 订单索引 + 外键
ALTER TABLE `credit_appeal`
    ADD INDEX `idx_order` (`order_id`);
ALTER TABLE `credit_appeal`
    ADD CONSTRAINT `fk_appeal_order` FOREIGN KEY (`order_id`) REFERENCES `dbs_order`(`id`);
