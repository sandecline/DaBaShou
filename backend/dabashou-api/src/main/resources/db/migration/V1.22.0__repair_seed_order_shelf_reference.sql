-- 修复 V1.18.0 种子订单 DBS20260630007 引用了不存在的货架 21。
-- React 前端开发货架实际 ID 为 17；只修复该条种子订单，避免影响真实订单。
UPDATE `dbs_order`
SET `skill_shelf_id` = 17
WHERE `id` = 16
  AND `order_no` = 'DBS20260630007'
  AND `skill_shelf_id` = 21
  AND EXISTS (SELECT 1 FROM `dbs_skill_shelf` WHERE `id` = 17);

-- 回滚语句：将该种子订单的货架关联清空，保留订单与其余业务数据。
-- UPDATE `dbs_order` SET `skill_shelf_id` = NULL
-- WHERE `id` = 16 AND `order_no` = 'DBS20260630007' AND `skill_shelf_id` = 17;
