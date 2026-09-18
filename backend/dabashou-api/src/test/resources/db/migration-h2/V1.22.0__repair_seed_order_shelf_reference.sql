-- H2 测试迁移：与 MySQL V1.22.0 保持版本及修复逻辑一致。
UPDATE `dbs_order`
SET `skill_shelf_id` = 17
WHERE `id` = 16
  AND `order_no` = 'DBS20260630007'
  AND `skill_shelf_id` = 21
  AND EXISTS (SELECT 1 FROM `dbs_skill_shelf` WHERE `id` = 17);

-- 回滚语句：将该种子订单的货架关联清空，保留订单与其余业务数据。
-- UPDATE `dbs_order` SET `skill_shelf_id` = NULL
-- WHERE `id` = 16 AND `order_no` = 'DBS20260630007' AND `skill_shelf_id` = 17;
