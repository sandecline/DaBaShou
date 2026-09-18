-- 为 credit_violation 表添加 handle_result 列，避免处理违规时覆盖原始 description
ALTER TABLE credit_violation ADD COLUMN handle_result VARCHAR(500) DEFAULT NULL COMMENT '处理结果说明' AFTER description;

-- rollback: ALTER TABLE credit_violation DROP COLUMN handle_result;
