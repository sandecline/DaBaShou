-- ============================================================================
-- 搭把手数据库迁移脚本 V1.12.0
-- 描述: 为用户表补充逻辑删除字段，匹配 BaseEntity 和 MyBatis-Plus 全局逻辑删除配置
-- 作者: dabashou-dev
-- 日期: 2026-06-30
-- 依赖: V1.0.0 (dbs_user)
-- 变更记录:
--   V1.12.0 (2026-06-30) - dbs_user 增加 deleted 字段
-- ============================================================================

ALTER TABLE `dbs_user`
    ADD COLUMN `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除: 0-未删除 1-已删除' AFTER `update_time`;

CREATE INDEX `dbs_user_idx_deleted` ON `dbs_user` (`deleted`);

-- 回滚语句:
-- DROP INDEX `dbs_user_idx_deleted` ON `dbs_user`;
-- ALTER TABLE `dbs_user` DROP COLUMN `deleted`;
