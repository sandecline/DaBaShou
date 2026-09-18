-- ============================================================================
-- 搭把手数据库迁移脚本 V1.16.0
-- 描述: 二轮整改 - 管理员角色配置、后台配置、评价软删除字段与复合索引
-- 日期: 2026-07-01
-- 依赖: V1.15.0
-- ============================================================================

INSERT IGNORE INTO `sys_role` (`role_code`, `role_name`, `description`, `status`)
VALUES ('ADMIN', '管理员', '平台后台管理员', 1),
       ('USER', '普通用户', '平台普通用户', 1);

ALTER TABLE `dbs_user`
    ADD COLUMN `token_version` INT NOT NULL DEFAULT 0 COMMENT '令牌版本号，用于禁用或重置密码后失效旧Token';

INSERT IGNORE INTO `sys_user_role` (`user_id`, `role_id`)
SELECT u.`id`, r.`id`
  FROM `dbs_user` u
  JOIN `sys_role` r ON r.`role_code` = 'ADMIN'
 WHERE u.`username` = 'admin';

INSERT IGNORE INTO `sys_user_role` (`user_id`, `role_id`)
SELECT u.`id`, r.`id`
  FROM `dbs_user` u
  JOIN `sys_role` r ON r.`role_code` = 'USER'
 WHERE NOT EXISTS (
       SELECT 1 FROM `sys_user_role` ur WHERE ur.`user_id` = u.`id` AND ur.`role_id` = r.`id`
 );

ALTER TABLE `dbs_review`
    ADD COLUMN `hidden` TINYINT NOT NULL DEFAULT 0 COMMENT '是否隐藏: 0-显示 1-隐藏';

ALTER TABLE `dbs_skill_shelf`
    ADD INDEX `idx_shelf_user_status` (`user_id`, `status`),
    ADD INDEX `idx_shelf_tag_status` (`skill_tag_id`, `status`);

ALTER TABLE `dbs_demand`
    ADD INDEX `idx_demand_user_status` (`user_id`, `status`),
    ADD INDEX `idx_demand_tag_status` (`skill_tag_id`, `status`);

ALTER TABLE `dbs_chat_message`
    ADD INDEX `idx_chat_message_session_time` (`session_id`, `create_time`);

ALTER TABLE `dbs_notification`
    ADD INDEX `idx_notification_user_read_time` (`user_id`, `is_read`, `create_time`);

INSERT IGNORE INTO `sys_config` (`config_key`, `config_value`, `config_name`, `config_type`, `description`)
VALUES ('site.name', '搭把手', '站点名称', 'business', '前台展示的平台名称'),
       ('site.notice', '', '站点公告', 'business', '首页或后台公告'),
       ('order.auto_cancel_minutes', '30', '订单自动取消分钟数', 'business', '待支付订单自动取消时间'),
       ('order.confirm_timeout_hours', '72', '确认超时时间', 'business', '服务核销后确认超时时间'),
       ('point.sign_in_reward', '5', '签到奖励积分', 'business', '每日签到奖励'),
       ('credit.violation_penalty', '0.5', '违规默认扣分', 'business', '管理员处理违规时的默认扣分');

-- ============================================================================
-- 数据核查 SQL（手机号唯一索引前置检查，需清洗后单独执行约束迁移）
-- ============================================================================
-- SELECT phone, COUNT(*) FROM dbs_user WHERE phone IS NOT NULL AND phone <> '' GROUP BY phone HAVING COUNT(*) > 1;
-- SELECT COUNT(*) FROM dbs_user WHERE phone IS NULL OR phone = '';
-- ALTER TABLE dbs_user ADD UNIQUE KEY uk_dbs_user_phone (phone);

-- ============================================================================
-- 回滚脚本
-- ============================================================================
-- ALTER TABLE `dbs_notification` DROP INDEX `idx_notification_user_read_time`;
-- ALTER TABLE `dbs_chat_message` DROP INDEX `idx_chat_message_session_time`;
-- ALTER TABLE `dbs_demand` DROP INDEX `idx_demand_tag_status`;
-- ALTER TABLE `dbs_demand` DROP INDEX `idx_demand_user_status`;
-- ALTER TABLE `dbs_skill_shelf` DROP INDEX `idx_shelf_tag_status`;
-- ALTER TABLE `dbs_skill_shelf` DROP INDEX `idx_shelf_user_status`;
-- ALTER TABLE `dbs_review` DROP COLUMN `hidden`;
-- ALTER TABLE `dbs_user` DROP COLUMN `token_version`;
-- DELETE FROM `sys_config` WHERE `config_key` IN ('site.name','site.notice','order.auto_cancel_minutes','order.confirm_timeout_hours','point.sign_in_reward','credit.violation_penalty');
