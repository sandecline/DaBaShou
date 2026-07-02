-- ============================================================================
-- 搭把手数据库迁移脚本 V1.16.0
-- 描述: 修复管理员账号与角色绑定
-- 作者: HuangYuYan
-- 日期: 2026-07-02
-- 依赖: V1.1.0 (sys_role, sys_user_role), V1.7.0 (seed_data)
-- 变更记录:
--   V1.16.0 (2026-07-02) - 确保 admin/123456 可登录并绑定 ADMIN 角色
-- ============================================================================

SET @admin_pwd_hash = '$2b$12$VipmeVvp066DBkrRvDNIgerpnmKqvo.lLzB84IwiIBlEahgG/0sjW';

INSERT IGNORE INTO `sys_role` (`role_code`, `role_name`, `description`, `status`)
VALUES
('ADMIN', '平台管理员', '管理所有用户、订单、信用审核、系统配置和平台统计', 1),
('USER', '学生用户', '校园互助平台普通用户', 1);

UPDATE `sys_role`
   SET `status` = 1,
       `update_time` = NOW()
 WHERE `role_code` IN ('ADMIN', 'USER');

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

-- ========== ROLLBACK ==========
-- DELETE ur FROM `sys_user_role` ur
-- JOIN `dbs_user` u ON u.id = ur.user_id
-- JOIN `sys_role` r ON r.id = ur.role_id
-- WHERE u.username = 'admin' AND r.role_code = 'ADMIN';
-- 注意: 不删除 admin 用户，避免破坏已有管理员数据。
