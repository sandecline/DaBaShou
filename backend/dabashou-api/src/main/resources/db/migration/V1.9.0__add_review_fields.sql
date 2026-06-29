-- ============================================================================
-- 搭把手数据库迁移脚本 V1.9.0
-- 描述: 评价表补充字段
-- ============================================================================

ALTER TABLE `dbs_review`
    ADD COLUMN `images` VARCHAR(1000) COMMENT '评价图片URL,JSON数组' AFTER `content`,
    ADD COLUMN `is_anonymous` TINYINT DEFAULT 0 COMMENT '是否匿名: 0-否 1-是' AFTER `images`;

-- ============================================================================
-- 回滚脚本
-- ============================================================================
-- ALTER TABLE `dbs_review` DROP COLUMN `images`, DROP COLUMN `is_anonymous`;
