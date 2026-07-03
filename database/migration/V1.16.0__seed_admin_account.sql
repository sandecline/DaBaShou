-- ============================================================================
-- 搭把手数据库迁移脚本 V1.16.0
-- 描述: 管理员角色配置、后台配置、评价软删除字段、复合索引、管理员账号种子
-- 日期: 2026-07-02
-- 依赖: V1.15.0
-- ============================================================================

-- 1. 角色数据
INSERT IGNORE INTO `sys_role` (`role_code`, `role_name`, `description`, `status`)
VALUES ('ADMIN', '管理员', '平台后台管理员', 1),
       ('USER', '普通用户', '平台普通用户', 1);

-- 2. dbs_user 加 token_version 字段
ALTER TABLE `dbs_user`
    ADD COLUMN `token_version` INT NOT NULL DEFAULT 0 COMMENT '令牌版本号，用于禁用或重置密码后失效旧Token';

-- 3. 管理员账号与角色绑定
SET @admin_pwd_hash = '$2b$12$VipmeVvp066DBkrRvDNIgerpnmKqvo.lLzB84IwiIBlEahgG/0sjW';

INSERT INTO `dbs_user` (`username`, `nickname`, `avatar`, `phone`, `password_hash`, `point_balance`, `trust_score`, `campus`, `building`, `bio`, `status`)
VALUES ('admin', '系统管理员', 'https://picsum.photos/seed/admin/200/200', '13800000000',
        @admin_pwd_hash, 99999, 5.0, '主校区', '行政楼', '搭把手平台管理员', 1)
ON DUPLICATE KEY UPDATE
    `password_hash` = @admin_pwd_hash,
    `nickname` = VALUES(`nickname`),
    `status` = 1,
    `update_time` = NOW();

INSERT IGNORE INTO `sys_user_role` (`user_id`, `role_id`)
SELECT u.id, r.id
  FROM `dbs_user` u
  JOIN `sys_role` r ON r.role_code = 'ADMIN'
 WHERE u.username = 'admin';

INSERT IGNORE INTO `sys_user_role` (`user_id`, `role_id`)
SELECT u.`id`, r.`id`
  FROM `dbs_user` u
  JOIN `sys_role` r ON r.`role_code` = 'USER'
 WHERE NOT EXISTS (
       SELECT 1 FROM `sys_user_role` ur WHERE ur.`user_id` = u.`id` AND ur.`role_id` = r.`id`
 );

-- 4. dbs_review 加 hidden 字段
ALTER TABLE `dbs_review`
    ADD COLUMN `hidden` TINYINT NOT NULL DEFAULT 0 COMMENT '是否隐藏: 0-显示 1-隐藏';

-- 5. 复合索引
ALTER TABLE `dbs_skill_shelf`
    ADD INDEX `idx_shelf_user_status` (`user_id`, `status`),
    ADD INDEX `idx_shelf_tag_status` (`skill_tag_id`, `status`);

ALTER TABLE `dbs_demand`
    ADD INDEX `idx_demand_user_status` (`user_id`, `status`),
    ADD INDEX `idx_demand_tag_status` (`skill_tag_id`, `status`);

ALTER TABLE `dbs_chat_message`
    ADD INDEX `idx_chat_message_session_time` (`session_id`, `create_time`);

ALTER TABLE `sys_notification`
    ADD INDEX `idx_sys_notification_user_read_time` (`user_id`, `is_read`, `create_time`);

-- 6. 系统配置
INSERT IGNORE INTO `sys_config` (`config_key`, `config_value`, `config_name`, `config_type`, `description`)
VALUES ('site.name', '搭把手', '站点名称', 'business', '前台展示的平台名称'),
       ('site.notice', '', '站点公告', 'business', '首页或后台公告'),
       ('order.auto_cancel_minutes', '30', '订单自动取消分钟数', 'business', '待支付订单自动取消时间'),
       ('order.confirm_timeout_hours', '72', '确认超时时间', 'business', '服务核销后确认超时时间'),
       ('point.sign_in_reward', '5', '签到奖励积分', 'business', '每日签到奖励'),
       ('credit.violation_penalty', '0.5', '违规默认扣分', 'business', '管理员处理违规时的默认扣分');

-- ========== ROLLBACK ==========
-- ALTER TABLE `sys_notification` DROP INDEX `idx_sys_notification_user_read_time`;
-- ALTER TABLE `dbs_chat_message` DROP INDEX `idx_chat_message_session_time`;
-- ALTER TABLE `dbs_demand` DROP INDEX `idx_demand_tag_status`;
-- ALTER TABLE `dbs_demand` DROP INDEX `idx_demand_user_status`;
-- ALTER TABLE `dbs_skill_shelf` DROP INDEX `idx_shelf_tag_status`;
-- ALTER TABLE `dbs_skill_shelf` DROP INDEX `idx_shelf_user_status`;
-- ALTER TABLE `dbs_review` DROP COLUMN `hidden`;
-- ALTER TABLE `dbs_user` DROP COLUMN `token_version`;
-- DELETE FROM `sys_config` WHERE `config_key` IN ('site.name','site.notice','order.auto_cancel_minutes','order.confirm_timeout_hours','point.sign_in_reward','credit.violation_penalty');
