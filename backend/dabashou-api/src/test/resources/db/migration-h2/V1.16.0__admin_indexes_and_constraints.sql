-- ============================================================================
-- 搭把手 H2 测试迁移脚本 V1.16.0
-- 描述: 二轮整改 - 管理员角色配置、后台配置、评价软删除字段与复合索引
-- ============================================================================

INSERT INTO `sys_role` (`role_code`, `role_name`, `description`, `status`)
VALUES ('ADMIN', '管理员', '平台后台管理员', 1),
       ('USER', '普通用户', '平台普通用户', 1);

ALTER TABLE `dbs_user`
    ADD COLUMN `token_version` INT NOT NULL DEFAULT 0 COMMENT '令牌版本号，用于禁用或重置密码后失效旧Token';

INSERT INTO `sys_user_role` (`user_id`, `role_id`)
SELECT u.`id`, r.`id`
  FROM `dbs_user` u
  JOIN `sys_role` r ON r.`role_code` = 'ADMIN'
 WHERE u.`username` = 'admin';

INSERT INTO `sys_user_role` (`user_id`, `role_id`)
SELECT u.`id`, r.`id`
  FROM `dbs_user` u
  JOIN `sys_role` r ON r.`role_code` = 'USER';

ALTER TABLE `dbs_review`
    ADD COLUMN `hidden` TINYINT NOT NULL DEFAULT 0 COMMENT '是否隐藏: 0-显示 1-隐藏';

CREATE INDEX `idx_shelf_user_status` ON `dbs_skill_shelf` (`user_id`, `status`);
CREATE INDEX `idx_shelf_tag_status` ON `dbs_skill_shelf` (`skill_tag_id`, `status`);
CREATE INDEX `idx_demand_user_status` ON `dbs_demand` (`user_id`, `status`);
CREATE INDEX `idx_demand_tag_status` ON `dbs_demand` (`skill_tag_id`, `status`);
CREATE INDEX `idx_chat_message_session_time` ON `dbs_chat_message` (`session_id`, `create_time`);
CREATE INDEX `idx_notification_user_read_time` ON `dbs_notification` (`user_id`, `is_read`, `create_time`);

INSERT INTO `sys_config` (`config_key`, `config_value`, `config_name`, `config_type`, `description`)
VALUES ('site.name', '搭把手', '站点名称', 'business', '前台展示的平台名称'),
       ('site.notice', '', '站点公告', 'business', '首页或后台公告'),
       ('order.auto_cancel_minutes', '30', '订单自动取消分钟数', 'business', '待支付订单自动取消时间'),
       ('order.confirm_timeout_hours', '72', '确认超时时间', 'business', '服务核销后确认超时时间'),
       ('point.sign_in_reward', '5', '签到奖励积分', 'business', '每日签到奖励'),
       ('credit.violation_penalty', '0.5', '违规默认扣分', 'business', '管理员处理违规时的默认扣分');
