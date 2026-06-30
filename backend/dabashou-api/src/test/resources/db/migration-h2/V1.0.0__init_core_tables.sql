-- ============================================================================
-- 搭把手数据库迁移脚本 V1.0.0
-- 描述: 初始化核心业务表（10张）
-- 作者: dabashou-dev
-- 日期: 2026-06-28
-- 变更记录:
--   V1.0.0 (2026-06-28) - 初始版本，创建核心业务表
-- ============================================================================

-- 用户表
CREATE TABLE IF NOT EXISTS `dbs_user` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '用户ID',
    `username` VARCHAR(50) NOT NULL UNIQUE COMMENT '用户名',
    `nickname` VARCHAR(50) COMMENT '昵称',
    `avatar` VARCHAR(255) COMMENT '头像URL',
    `phone` VARCHAR(20) COMMENT '手机号',
    `email` VARCHAR(100) COMMENT '邮箱',
    `password_hash` VARCHAR(255) NOT NULL COMMENT '密码哈希',
    `point_balance` INT DEFAULT 0 COMMENT '积分余额',
    `trust_score` DECIMAL(3,1) DEFAULT 5.0 COMMENT '信任分(1.0-5.0)',
    `longitude` DECIMAL(10,7) COMMENT '经度',
    `latitude` DECIMAL(10,7) COMMENT '纬度',
    `campus` VARCHAR(50) COMMENT '校区',
    `building` VARCHAR(50) COMMENT '楼栋',
    `bio` VARCHAR(500) COMMENT '个人简介',
    `status` TINYINT DEFAULT 1 COMMENT '状态: 0-禁用 1-正常',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX `dbs_user_idx_location` (`longitude`, `latitude`),
    INDEX `dbs_user_idx_campus` (`campus`),
    INDEX `dbs_user_idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 技能分类表
CREATE TABLE IF NOT EXISTS `dbs_skill_category` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '分类ID',
    `name` VARCHAR(50) NOT NULL COMMENT '分类名称',
    `icon` VARCHAR(100) COMMENT '图标',
    `sort_order` INT DEFAULT 0 COMMENT '排序',
    `status` TINYINT DEFAULT 1 COMMENT '状态: 0-禁用 1-正常',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='技能分类表';

-- 技能标签表
CREATE TABLE IF NOT EXISTS `dbs_skill_tag` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '标签ID',
    `category_id` BIGINT NOT NULL COMMENT '分类ID',
    `name` VARCHAR(50) NOT NULL COMMENT '标签名称',
    `status` TINYINT DEFAULT 1 COMMENT '状态: 0-禁用 1-正常',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    FOREIGN KEY (`category_id`) REFERENCES `dbs_skill_category`(`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='技能标签表';

-- 用户技能表
CREATE TABLE IF NOT EXISTS `dbs_user_skill` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `skill_tag_id` BIGINT NOT NULL COMMENT '技能标签ID',
    `proficiency` TINYINT DEFAULT 1 COMMENT '熟练度: 1-了解 2-熟悉 3-精通 4-专家',
    `description` VARCHAR(500) COMMENT '技能描述',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    UNIQUE KEY `dbs_user_skill_uk_user_skill` (`user_id`, `skill_tag_id`),
    FOREIGN KEY (`user_id`) REFERENCES `dbs_user`(`id`),
    FOREIGN KEY (`skill_tag_id`) REFERENCES `dbs_skill_tag`(`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户技能表';

-- 时间格子表
CREATE TABLE IF NOT EXISTS `dbs_time_slot` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `date` DATE NOT NULL COMMENT '日期',
    `start_time` TIME NOT NULL COMMENT '开始时间',
    `end_time` TIME NOT NULL COMMENT '结束时间',
    `status` TINYINT DEFAULT 1 COMMENT '状态: 0-不可用 1-可预约 2-已预约',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX `dbs_time_slot_idx_user_date` (`user_id`, `date`),
    FOREIGN KEY (`user_id`) REFERENCES `dbs_user`(`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='时间格子表';

-- 技能货架表
CREATE TABLE IF NOT EXISTS `dbs_skill_shelf` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '货架ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `skill_tag_id` BIGINT NOT NULL COMMENT '技能标签ID',
    `title` VARCHAR(100) NOT NULL COMMENT '服务标题',
    `description` TEXT COMMENT '服务描述',
    `point_price` INT NOT NULL COMMENT '积分价格',
    `duration_minutes` INT COMMENT '预计时长(分钟)',
    `location_type` TINYINT DEFAULT 1 COMMENT '地点类型: 1-线上 2-线下 3-均可',
    `status` TINYINT DEFAULT 1 COMMENT '状态: 0-下架 1-上架 2-审核中',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX `dbs_skill_shelf_idx_user` (`user_id`),
    INDEX `dbs_skill_shelf_idx_skill` (`skill_tag_id`),
    INDEX `dbs_skill_shelf_idx_status` (`status`),
    FOREIGN KEY (`user_id`) REFERENCES `dbs_user`(`id`),
    FOREIGN KEY (`skill_tag_id`) REFERENCES `dbs_skill_tag`(`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='技能货架表';

-- 需求表
CREATE TABLE IF NOT EXISTS `dbs_demand` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '需求ID',
    `user_id` BIGINT NOT NULL COMMENT '发布者ID',
    `skill_tag_id` BIGINT COMMENT '所需技能标签ID',
    `title` VARCHAR(100) NOT NULL COMMENT '需求标题',
    `description` TEXT COMMENT '需求描述',
    `point_reward` INT NOT NULL COMMENT '悬赏积分',
    `deadline` DATETIME COMMENT '截止时间',
    `location_type` TINYINT DEFAULT 1 COMMENT '地点类型: 1-线上 2-线下 3-均可',
    `longitude` DECIMAL(10,7) COMMENT '经度',
    `latitude` DECIMAL(10,7) COMMENT '纬度',
    `campus` VARCHAR(50) COMMENT '校区',
    `building` VARCHAR(50) COMMENT '楼栋',
    `status` TINYINT DEFAULT 1 COMMENT '状态: 0-已关闭 1-待接单 2-进行中 3-已完成',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX `dbs_demand_idx_user` (`user_id`),
    INDEX `dbs_demand_idx_skill` (`skill_tag_id`),
    INDEX `dbs_demand_idx_status` (`status`),
    INDEX `dbs_demand_idx_location` (`longitude`, `latitude`),
    FOREIGN KEY (`user_id`) REFERENCES `dbs_user`(`id`),
    FOREIGN KEY (`skill_tag_id`) REFERENCES `dbs_skill_tag`(`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='需求表';

-- 订单表
CREATE TABLE IF NOT EXISTS `dbs_order` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '订单ID',
    `order_no` VARCHAR(32) NOT NULL UNIQUE COMMENT '订单号',
    `buyer_id` BIGINT NOT NULL COMMENT '买家ID',
    `seller_id` BIGINT NOT NULL COMMENT '卖家ID',
    `demand_id` BIGINT COMMENT '关联需求ID',
    `skill_shelf_id` BIGINT COMMENT '关联货架ID',
    `skill_tag_id` BIGINT NOT NULL COMMENT '技能标签ID',
    `title` VARCHAR(100) NOT NULL COMMENT '服务标题',
    `point_amount` INT NOT NULL COMMENT '积分金额',
    `status` TINYINT NOT NULL COMMENT '状态: 0-已取消 1-待支付 2-已支付(担保中) 3-服务中 4-待确认 5-已完成 6-已退款 7-争议中',
    `verify_code` VARCHAR(10) COMMENT '核销码',
    `verify_code_expire` DATETIME COMMENT '核销码过期时间',
    `time_slot_id` BIGINT COMMENT '预约的时间格子ID',
    `service_start_time` DATETIME COMMENT '服务开始时间',
    `service_end_time` DATETIME COMMENT '服务结束时间',
    `complete_time` DATETIME COMMENT '完成时间',
    `cancel_time` DATETIME COMMENT '取消时间',
    `cancel_reason` VARCHAR(200) COMMENT '取消原因',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX `dbs_order_idx_buyer` (`buyer_id`),
    INDEX `dbs_order_idx_seller` (`seller_id`),
    INDEX `dbs_order_idx_status` (`status`),
    INDEX `dbs_order_idx_order_no` (`order_no`),
    FOREIGN KEY (`buyer_id`) REFERENCES `dbs_user`(`id`),
    FOREIGN KEY (`seller_id`) REFERENCES `dbs_user`(`id`),
    FOREIGN KEY (`demand_id`) REFERENCES `dbs_demand`(`id`),
    FOREIGN KEY (`skill_shelf_id`) REFERENCES `dbs_skill_shelf`(`id`),
    FOREIGN KEY (`skill_tag_id`) REFERENCES `dbs_skill_tag`(`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单表';

-- 积分流水表
CREATE TABLE IF NOT EXISTS `dbs_point_transaction` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '流水ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `order_id` BIGINT COMMENT '关联订单ID',
    `type` TINYINT NOT NULL COMMENT '类型: 1-收入 2-支出 3-冻结 4-解冻 5-系统奖励 6-系统扣除',
    `amount` INT NOT NULL COMMENT '积分数量',
    `balance_after` INT NOT NULL COMMENT '变动后余额',
    `description` VARCHAR(200) COMMENT '描述',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX `dbs_point_transaction_idx_user` (`user_id`),
    INDEX `dbs_point_transaction_idx_order` (`order_id`),
    INDEX `dbs_point_transaction_idx_type` (`type`),
    FOREIGN KEY (`user_id`) REFERENCES `dbs_user`(`id`),
    FOREIGN KEY (`order_id`) REFERENCES `dbs_order`(`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='积分流水表';

-- 评价表
CREATE TABLE IF NOT EXISTS `dbs_review` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '评价ID',
    `order_id` BIGINT NOT NULL COMMENT '订单ID',
    `reviewer_id` BIGINT NOT NULL COMMENT '评价者ID',
    `reviewee_id` BIGINT NOT NULL COMMENT '被评价者ID',
    `rating` TINYINT NOT NULL COMMENT '评分: 1-5',
    `content` VARCHAR(500) COMMENT '评价内容',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    UNIQUE KEY `dbs_review_uk_order_reviewer` (`order_id`, `reviewer_id`),
    FOREIGN KEY (`order_id`) REFERENCES `dbs_order`(`id`),
    FOREIGN KEY (`reviewer_id`) REFERENCES `dbs_user`(`id`),
    FOREIGN KEY (`reviewee_id`) REFERENCES `dbs_user`(`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='评价表';
