-- ============================================================================
-- 搭把手数据库迁移脚本 V1.13.0
-- 描述: 修复早期本地库中测试用户的占位密码哈希
-- 作者: dabashou-dev
-- 日期: 2026-06-30
-- 依赖: V1.7.0 (seed data)
-- 变更记录:
--   V1.13.0 (2026-06-30) - 将占位 password_hash 修复为 BCrypt 哈希
-- ============================================================================

UPDATE `dbs_user`
SET `password_hash` = CASE `username`
    WHEN 'zhangsan' THEN '$2b$12$VipmeVvp066DBkrRvDNIgerpnmKqvo.lLzB84IwiIBlEahgG/0sjW'
    WHEN 'lisi' THEN '$2b$12$QVF7Ab0Isg0OsT1HNbCEOOiaNiC7q50MLXOwNXNgG3Ojs3JNDKd6G'
    WHEN 'wangwu' THEN '$2b$12$BrHm3.KTC8iyQ9PFmz6WOubq8QT47ofPcCIsFP6Dc7q1TyFAZqARO'
    ELSE `password_hash`
END
WHERE `username` IN ('zhangsan', 'lisi', 'wangwu')
  AND `password_hash` LIKE '$2a$10$dummy%';

-- 回滚语句:
-- UPDATE `dbs_user` SET `password_hash` = '$2a$10$dummyhash' WHERE `username` IN ('zhangsan', 'lisi', 'wangwu');
