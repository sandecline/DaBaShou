-- H2 测试迁移：与 MySQL V1.21.0 保持订单争议字段一致。
ALTER TABLE `dbs_order` ADD COLUMN `dispute_reason` VARCHAR(500) DEFAULT NULL COMMENT '争议原因';
ALTER TABLE `dbs_order` ADD COLUMN `dispute_explain` VARCHAR(500) DEFAULT NULL COMMENT '争议补充说明';
ALTER TABLE `dbs_order` ADD COLUMN `dispute_user_id` BIGINT DEFAULT NULL COMMENT '争议发起人用户ID';
ALTER TABLE `dbs_order` ADD COLUMN `dispute_time` DATETIME DEFAULT NULL COMMENT '争议发起时间';
CREATE INDEX `idx_dispute_user` ON `dbs_order` (`dispute_user_id`);

-- 回滚：先删除索引，再依次删除争议字段。
-- DROP INDEX `idx_dispute_user`;
-- ALTER TABLE `dbs_order` DROP COLUMN `dispute_time`;
-- ALTER TABLE `dbs_order` DROP COLUMN `dispute_user_id`;
-- ALTER TABLE `dbs_order` DROP COLUMN `dispute_explain`;
-- ALTER TABLE `dbs_order` DROP COLUMN `dispute_reason`;
