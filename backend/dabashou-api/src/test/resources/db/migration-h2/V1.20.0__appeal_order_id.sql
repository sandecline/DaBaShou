-- H2 测试迁移：与 MySQL V1.20.0 保持申诉表结构一致。
ALTER TABLE `credit_appeal` ADD COLUMN `order_id` BIGINT COMMENT '关联订单ID';
ALTER TABLE `credit_appeal` ALTER COLUMN `violation_id` BIGINT NULL;
CREATE INDEX `idx_appeal_order` ON `credit_appeal` (`order_id`);
ALTER TABLE `credit_appeal` ADD CONSTRAINT `fk_appeal_order` FOREIGN KEY (`order_id`) REFERENCES `dbs_order`(`id`);

-- 回滚：先删除外键和索引，再删除 order_id；已有空 violation_id 时须先处理数据。
-- ALTER TABLE `credit_appeal` DROP CONSTRAINT `fk_appeal_order`;
-- DROP INDEX `idx_appeal_order`;
-- ALTER TABLE `credit_appeal` DROP COLUMN `order_id`;
