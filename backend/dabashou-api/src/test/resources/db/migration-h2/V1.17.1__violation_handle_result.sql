-- 为 credit_violation 表添加 handle_result 列
ALTER TABLE credit_violation ADD COLUMN handle_result VARCHAR(500) DEFAULT NULL COMMENT '处理结果说明';
