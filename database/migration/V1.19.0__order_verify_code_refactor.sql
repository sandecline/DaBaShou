-- ============================================================================
-- 搭把手数据库迁移脚本 V1.19.0
-- 描述: 订单核销码流程改造 — 支持双方核销码驱动的状态流转
-- 作者: dabashou-dev
-- 日期: 2026-07-03
-- 依赖: V1.0.0 (dbs_order 表已存在)
-- ============================================================================

-- 添加新列（使用存储过程实现幂等）
DELIMITER //
CREATE PROCEDURE IF NOT EXISTS add_column_if_not_exists(
    IN p_table VARCHAR(64), IN p_column VARCHAR(64), IN p_definition VARCHAR(512)
)
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_schema = DATABASE() AND table_name = p_table AND column_name = p_column
    ) THEN
        SET @sql = CONCAT('ALTER TABLE `', p_table, '` ADD COLUMN `', p_column, '` ', p_definition);
        PREPARE stmt FROM @sql;
        EXECUTE stmt;
        DEALLOCATE PREPARE stmt;
    END IF;
END //
DELIMITER ;

-- 开始核销码（双方各一个，订单创建时生成）
CALL add_column_if_not_exists('dbs_order', 'buyer_verify_code', 'VARCHAR(6) DEFAULT NULL COMMENT ''开始核销码-买家持有''');
CALL add_column_if_not_exists('dbs_order', 'seller_verify_code', 'VARCHAR(6) DEFAULT NULL COMMENT ''开始核销码-卖家持有''');

-- 完成确认码（双方各一个，服务开始后生成）
CALL add_column_if_not_exists('dbs_order', 'buyer_confirm_code', 'VARCHAR(6) DEFAULT NULL COMMENT ''完成确认码-买家持有''');
CALL add_column_if_not_exists('dbs_order', 'seller_confirm_code', 'VARCHAR(6) DEFAULT NULL COMMENT ''完成确认码-卖家持有''');

-- 核销确认状态（0=未核销，1=已核销）
CALL add_column_if_not_exists('dbs_order', 'buyer_verified', 'TINYINT DEFAULT 0 COMMENT ''买家是否已输入核销码''');
CALL add_column_if_not_exists('dbs_order', 'seller_verified', 'TINYINT DEFAULT 0 COMMENT ''卖家是否已输入核销码''');
CALL add_column_if_not_exists('dbs_order', 'buyer_confirmed', 'TINYINT DEFAULT 0 COMMENT ''买家是否已输入确认码''');
CALL add_column_if_not_exists('dbs_order', 'seller_confirmed', 'TINYINT DEFAULT 0 COMMENT ''卖家是否已输入确认码''');

-- 退款相关
CALL add_column_if_not_exists('dbs_order', 'refund_requester', 'VARCHAR(10) DEFAULT NULL COMMENT ''退款发起方: buyer/seller''');
CALL add_column_if_not_exists('dbs_order', 'refund_agreed', 'TINYINT DEFAULT 0 COMMENT ''退款是否已同意''');

-- 清理存储过程
DROP PROCEDURE IF EXISTS add_column_if_not_exists;

-- 回滚语句:
-- ALTER TABLE dbs_order
--   DROP COLUMN buyer_verify_code,
--   DROP COLUMN seller_verify_code,
--   DROP COLUMN buyer_confirm_code,
--   DROP COLUMN seller_confirm_code,
--   DROP COLUMN buyer_verified,
--   DROP COLUMN seller_verified,
--   DROP COLUMN buyer_confirmed,
--   DROP COLUMN seller_confirmed,
--   DROP COLUMN refund_requester,
--   DROP COLUMN refund_agreed;
