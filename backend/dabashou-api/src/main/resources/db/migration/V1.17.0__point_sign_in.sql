-- ============================================================================
-- Dabashou database migration V1.17.0
-- Description: Add daily point sign-in records
-- Date: 2026-07-02
-- Depends on: V1.16.0
-- ============================================================================

CREATE TABLE IF NOT EXISTS `dbs_point_sign_in` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '签到记录ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `sign_date` DATE NOT NULL COMMENT '签到日期',
    `reward` INT NOT NULL COMMENT '签到奖励积分',
    `consecutive_days` INT NOT NULL DEFAULT 1 COMMENT '连续签到天数',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    UNIQUE KEY `uk_point_sign_in_user_date` (`user_id`, `sign_date`),
    INDEX `idx_point_sign_in_user_date` (`user_id`, `sign_date`),
    CONSTRAINT `fk_point_sign_in_user` FOREIGN KEY (`user_id`) REFERENCES `dbs_user`(`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='积分签到记录表';

-- ============================================================================
-- Rollback
-- ============================================================================
-- DROP TABLE IF EXISTS `dbs_point_sign_in`;

