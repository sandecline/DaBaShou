-- ============================================================================
-- 搭把手数据库迁移脚本 V1.6.0
-- 描述: 修复索引、约束、逻辑删除字段
-- 作者: dabashou-dev
-- 日期: 2026-06-29
-- 依赖: V1.0.0 (dbs_order, dbs_demand), V1.4.0 (dbs_chat_message)
-- 变更记录:
--   V1.6.0 (2026-06-29) - 补复合索引、verify_code加唯一约束、dbs_order加deleted字段
-- ============================================================================

-- 1. dbs_order 加复合索引（高频组合查询）
ALTER TABLE `dbs_order`
    ADD INDEX `idx_buyer_status` (`buyer_id`, `status`);

ALTER TABLE `dbs_order`
    ADD INDEX `idx_seller_status` (`seller_id`, `status`);

-- 2. dbs_order.verify_code 加唯一约束（防核销码重复）
ALTER TABLE `dbs_order`
    ADD UNIQUE KEY `uk_verify_code` (`verify_code`);

-- 3. dbs_order 加 deleted 字段（配合全局逻辑删除）
ALTER TABLE `dbs_order`
    ADD COLUMN `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除: 0-未删除 1-已删除' AFTER `update_time`;

-- 4. dbs_demand 加复合索引（按状态+类型查询）
ALTER TABLE `dbs_demand`
    ADD INDEX `idx_status_type` (`status`, `demand_type`);

-- 5. dbs_chat_message 加复合索引（按会话+时间查询最新消息）
ALTER TABLE `dbs_chat_message`
    ADD INDEX `idx_session_time` (`session_id`, `create_time`);

-- ========== ROLLBACK ==========
-- 回滚步骤:
-- ALTER TABLE `dbs_order` DROP INDEX `idx_buyer_status`;
-- ALTER TABLE `dbs_order` DROP INDEX `idx_seller_status`;
-- ALTER TABLE `dbs_order` DROP INDEX `uk_verify_code`;
-- ALTER TABLE `dbs_order` DROP COLUMN `deleted`;
-- ALTER TABLE `dbs_demand` DROP INDEX `idx_status_type`;
-- ALTER TABLE `dbs_chat_message` DROP INDEX `idx_session_time`;
