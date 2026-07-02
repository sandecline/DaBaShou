package com.dabashou.api.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * @projectName: nplab-backend
 * @package: com.dabashou.api.config
 * @className: AdminAccountInitializer
 * @author: HuangYuYan
 * @description: 启动时幂等修复默认管理员账号和角色绑定
 * @date: 2026-07-02 14:58
 * @version: 1.0
 */
@Component
public class AdminAccountInitializer implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(AdminAccountInitializer.class);

    private static final String ADMIN_USERNAME = "admin";
    private static final String ADMIN_PASSWORD = "123456";

    private final JdbcTemplate jdbcTemplate;
    private final PasswordEncoder passwordEncoder;

    public AdminAccountInitializer(JdbcTemplate jdbcTemplate, PasswordEncoder passwordEncoder) {
        this.jdbcTemplate = jdbcTemplate;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(ApplicationArguments args) {
        try {
            ensureRoles();
            ensureAdminUser();
            ensureAdminRole();
            log.info("默认管理员账号已就绪: username={}", ADMIN_USERNAME);
        } catch (DataAccessException e) {
            log.warn("默认管理员账号初始化跳过，请检查数据库表是否已创建: {}", e.getMessage());
        }
    }

    private void ensureRoles() {
        jdbcTemplate.update("""
                INSERT IGNORE INTO sys_role (role_code, role_name, description, status)
                VALUES
                ('ADMIN', '平台管理员', '管理所有用户、订单、信用审核、系统配置和平台统计', 1),
                ('USER', '学生用户', '校园互助平台普通用户', 1)
                """);
        jdbcTemplate.update("""
                UPDATE sys_role
                   SET status = 1, update_time = NOW()
                 WHERE role_code IN ('ADMIN', 'USER')
                """);
    }

    private void ensureAdminUser() {
        String passwordHash = passwordEncoder.encode(ADMIN_PASSWORD);
        jdbcTemplate.update("""
                INSERT INTO dbs_user (username, nickname, avatar, phone, password_hash, point_balance, trust_score, campus, building, bio, status)
                VALUES (?, '系统管理员', 'https://picsum.photos/seed/admin/200/200', '13800000000', ?, 99999, 5.0, '主校区', '行政楼', '搭把手平台管理员', 1)
                ON DUPLICATE KEY UPDATE
                    password_hash = VALUES(password_hash),
                    nickname = VALUES(nickname),
                    status = 1,
                    update_time = NOW()
                """, ADMIN_USERNAME, passwordHash);
    }

    private void ensureAdminRole() {
        jdbcTemplate.update("""
                INSERT IGNORE INTO sys_user_role (user_id, role_id)
                SELECT u.id, r.id
                  FROM dbs_user u
                  JOIN sys_role r ON r.role_code = 'ADMIN'
                 WHERE u.username = ?
                """, ADMIN_USERNAME);
    }
}
