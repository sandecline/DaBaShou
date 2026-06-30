-- H2测试库补齐BaseEntity逻辑删除字段
ALTER TABLE `dbs_user`
    ADD COLUMN `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除: 0-未删除 1-已删除';

-- H2测试库初始化积分账户，保证订单支付链路可重复执行
INSERT INTO `dbs_point_account` (`user_id`, `available`, `frozen`, `total_earned`, `total_spent`) VALUES
(1, 99999, 0, 0, 0),
(2, 500, 0, 0, 0),
(3, 300, 0, 0, 0),
(4, 200, 0, 0, 0);
