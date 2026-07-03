-- ============================================================================
-- 搭把手数据库迁移脚本 V1.18.0
-- 描述: 完整种子数据 — 补充缺失 schema，修复所有已知 bug，使用相对日期
-- 作者: dabashou-dev
-- 日期: 2026-07-02
-- 依赖: V1.0.0 ~ V1.17.0
-- 说明: 幂等脚本，可重复执行。先清理再重建。
-- ============================================================================

SET FOREIGN_KEY_CHECKS = 0;
SET NAMES utf8mb4;

-- ==================== 0. 补充缺失 Schema ====================

-- 创建签到表（V1.17.0 可能未执行）
CREATE TABLE IF NOT EXISTS `dbs_point_sign_in` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '签到ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `sign_date` date NOT NULL COMMENT '签到日期',
  `reward` int NOT NULL DEFAULT 5 COMMENT '奖励积分',
  `consecutive_days` int NOT NULL DEFAULT 1 COMMENT '连续签到天数',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_date` (`user_id`, `sign_date`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='积分签到记录';

-- 添加缺失字段（V1.16.0/V1.17.1 可能未执行）
-- 使用存储过程实现条件 ALTER
DELIMITER $$
DROP PROCEDURE IF EXISTS `add_column_if_missing`$$
CREATE PROCEDURE `add_column_if_missing`(
    IN p_table VARCHAR(64), IN p_column VARCHAR(64), IN p_definition TEXT
)
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS
        WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = p_table AND COLUMN_NAME = p_column
    ) THEN
        SET @sql = CONCAT('ALTER TABLE `', p_table, '` ADD COLUMN `', p_column, '` ', p_definition);
        PREPARE stmt FROM @sql;
        EXECUTE stmt;
        DEALLOCATE PREPARE stmt;
    END IF;
END$$
DELIMITER ;

CALL add_column_if_missing('dbs_review', 'hidden', 'tinyint DEFAULT 0 COMMENT ''是否隐藏''');
CALL add_column_if_missing('credit_violation', 'handle_result', 'varchar(500) DEFAULT NULL COMMENT ''处理结果''');
CALL add_column_if_missing('dbs_user', 'token_version', 'int NOT NULL DEFAULT 0 COMMENT ''Token版本号''');

-- 清理存储过程
DROP PROCEDURE IF EXISTS `add_column_if_missing`;

-- ==================== 1. 清理所有种子数据 ====================

TRUNCATE TABLE `sys_log`;
TRUNCATE TABLE `stat_skill_heat`;
TRUNCATE TABLE `stat_daily_summary`;
TRUNCATE TABLE `dbs_stat_skill_heat`;
TRUNCATE TABLE `dbs_stat_daily`;
TRUNCATE TABLE `dbs_time_slot`;
TRUNCATE TABLE `dbs_point_sign_in`;
TRUNCATE TABLE `user_trust_score_log`;
TRUNCATE TABLE `user_campus_auth`;
TRUNCATE TABLE `dbs_user_trust_score_log`;
TRUNCATE TABLE `dbs_user_campus_auth`;
TRUNCATE TABLE `dbs_notification`;
TRUNCATE TABLE `sys_notification`;
TRUNCATE TABLE `credit_appeal`;
TRUNCATE TABLE `credit_violation`;
TRUNCATE TABLE `dbs_chat_message`;
TRUNCATE TABLE `dbs_chat_session`;
TRUNCATE TABLE `dbs_review`;
TRUNCATE TABLE `dbs_guarantee_pool`;
TRUNCATE TABLE `dbs_point_freeze`;
TRUNCATE TABLE `dbs_point_transaction`;
TRUNCATE TABLE `dbs_point_account`;
TRUNCATE TABLE `dbs_order`;
TRUNCATE TABLE `dbs_demand`;
TRUNCATE TABLE `dbs_skill_shelf`;
TRUNCATE TABLE `dbs_user_skill`;
TRUNCATE TABLE `sys_role_permission`;
TRUNCATE TABLE `sys_permission`;
TRUNCATE TABLE `sys_user_role`;
TRUNCATE TABLE `sys_role`;
TRUNCATE TABLE `sys_config`;
DELETE FROM `dbs_user` WHERE `id` > 0;

-- ==================== 2. 用户数据 (24人，显式ID) ====================

SET @pwd = '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi';

INSERT INTO `dbs_user` (`id`, `username`, `nickname`, `avatar`, `phone`, `password_hash`, `point_balance`, `trust_score`, `campus`, `building`, `bio`, `status`, `longitude`, `latitude`) VALUES
(1, 'admin', '管理员', NULL, '13800000000', @pwd, 99999, 5.0, NULL, NULL, '系统管理员', 1, NULL, NULL),
(2, 'zhangsan', '张三', 'https://api.dicebear.com/7.x/avataaars/svg?seed=zhangsan', '13800000001', @pwd, 200, 4.5, '主校区', '1号宿舍楼', '计算机学院大四，擅长Python和Java开发', 1, 116.397428, 39.90923),
(3, 'lisi', '李四', 'https://api.dicebear.com/7.x/avataaars/svg?seed=lisi', '13800000002', @pwd, 500, 4.8, '东校区', '2号宿舍楼', '设计学院大三，海报/UI设计达人', 1, 116.40528, 39.90421),
(4, 'wangwu', '王五', 'https://api.dicebear.com/7.x/avataaars/svg?seed=wangwu', '13800000003', @pwd, 300, 4.2, '西校区', '3号宿舍楼', '体育学院，篮球校队主力', 1, 116.38915, 39.91234),
(5, 'chenqi', '陈七', 'https://api.dicebear.com/7.x/avataaars/svg?seed=chenqi', '13800000004', @pwd, 350, 4.8, '主校区', '4号宿舍楼', '计算机学院大三，ACM银牌', 1, 116.397428, 39.90923),
(6, 'zhaoba', '赵八', 'https://api.dicebear.com/7.x/avataaars/svg?seed=zhaoba', '13800000005', @pwd, 180, 3.5, '东校区', '5号宿舍楼', '艺术学院，喜欢画画和摄影', 1, 116.40528, 39.90421),
(7, 'sunjiu', '孙九', 'https://api.dicebear.com/7.x/avataaars/svg?seed=sunjiu', '13800000006', @pwd, 420, 4.2, '主校区', '6号宿舍楼', '数学系学霸，高数满分', 1, 116.39812, 39.91015),
(8, 'zhouyi', '周十', 'https://api.dicebear.com/7.x/avataaars/svg?seed=zhouyi', '13800000007', @pwd, 90, 2.8, '西校区', '1号宿舍楼', '大一新生，啥都不会但很爱学', 1, 116.38915, 39.91234),
(9, 'wuer', '吴二', 'https://api.dicebear.com/7.x/avataaars/svg?seed=wuer', '13800000008', @pwd, 600, 4.9, '主校区', '7号宿舍楼', '研究生，精通深度学习', 1, 116.39688, 39.90856),
(10, 'zhengsan', '郑三', 'https://api.dicebear.com/7.x/avataaars/svg?seed=zhengsan', '13800000009', @pwd, 250, 3.8, '东校区', '2号宿舍楼', '音乐学院，吉他十级', 1, 116.40455, 39.90378),
(11, 'huangsi', '黄四', 'https://api.dicebear.com/7.x/avataaars/svg?seed=huangsi', '13800000010', @pwd, 150, 3.2, '主校区', '8号宿舍楼', '体育生，篮球校队', 1, 116.39777, 39.90989),
(12, 'linwu', '林五', 'https://api.dicebear.com/7.x/avataaars/svg?seed=linwu', '13800000011', @pwd, 380, 4.5, '西校区', '2号宿舍楼', '英语专业，专八优秀', 1, 116.38892, 39.91198),
(13, 'hexiao', '何小', 'https://api.dicebear.com/7.x/avataaars/svg?seed=hexiao', '13800000012', @pwd, 200, 3.9, '主校区', '9号宿舍楼', '设计学院，UI/UX设计', 1, 116.39801, 39.90765),
(14, 'luda', '卢大', 'https://api.dicebear.com/7.x/avataaars/svg?seed=luda', '13800000013', @pwd, 500, 4.7, '东校区', '3号宿舍楼', '全栈工程师，实习字节', 1, 116.40388, 39.90456),
(15, 'xietian', '谢天', 'https://api.dicebear.com/7.x/avataaars/svg?seed=xietian', '13800000014', @pwd, 120, 3.0, '主校区', '10号宿舍楼', '化学系，实验室常客', 1, 116.39712, 39.91078),
(16, 'caoxi', '曹曦', 'https://api.dicebear.com/7.x/avataaars/svg?seed=caoxi', '13800000015', @pwd, 280, 4.1, '西校区', '3号宿舍楼', '法学院，辩论队队长', 1, 116.38978, 39.91145),
(17, 'gaoyun', '高云', 'https://api.dicebear.com/7.x/avataaars/svg?seed=gaoyun', '13800000016', @pwd, 350, 4.4, '主校区', '11号宿舍楼', '物理系，量子计算方向', 1, 116.39845, 39.90812),
(18, 'tangmin', '唐敏', 'https://api.dicebear.com/7.x/avataaars/svg?seed=tangmin', '13800000017', @pwd, 400, 4.6, '东校区', '4号宿舍楼', '医学院，解剖课助教', 1, 116.40278, 39.90512),
(19, 'hanmei', '韩梅', 'https://api.dicebear.com/7.x/avataaars/svg?seed=hanmei', '13800000018', @pwd, 180, 3.6, '主校区', '12号宿舍楼', '新闻学院，校报记者', 1, 116.39655, 39.91134),
(20, 'fengyi', '冯一', 'https://api.dicebear.com/7.x/avataaars/svg?seed=fengyi', '13800000019', @pwd, 550, 4.8, '西校区', '4号宿舍楼', '经济学院，GPA3.9', 1, 116.39012, 39.91089),
(21, 'dengchao', '邓超', 'https://api.dicebear.com/7.x/avataaars/svg?seed=dengchao', '13800000020', @pwd, 300, 4.0, '主校区', '13号宿舍楼', '机械学院，机器人社团', 1, 116.39788, 39.90945),
(22, 'pengpeng', '彭鹏', 'https://api.dicebear.com/7.x/avataaars/svg?seed=pengpeng', '13800000021', @pwd, 100, 2.5, '东校区', '5号宿舍楼', '历史系，古籍爱好者', 1, 116.40412, 39.90345),
(23, 'xiaohua', '小花', 'https://api.dicebear.com/7.x/avataaars/svg?seed=xiaohua', '13800000022', @pwd, 450, 4.3, '主校区', '14号宿舍楼', '外国语学院，日语N1', 1, 116.39698, 39.91023),
(24, 'dawei', '大卫', 'https://api.dicebear.com/7.x/avataaars/svg?seed=dawei', '13800000023', @pwd, 320, 4.1, '西校区', '5号宿舍楼', '建筑学院，手绘达人', 1, 116.38945, 39.91178);

ALTER TABLE `dbs_user` AUTO_INCREMENT = 25;

-- ==================== 3. 角色与权限 ====================

INSERT INTO `sys_role` (`id`, `role_code`, `role_name`, `description`, `status`) VALUES
(1, 'ADMIN', '管理员', '平台后台管理员', 1),
(2, 'USER', '普通用户', '平台普通用户', 1);

INSERT INTO `sys_user_role` (`user_id`, `role_id`) VALUES
(1, 1), (2, 2), (3, 2), (4, 2), (5, 2), (6, 2), (7, 2), (8, 2), (9, 2), (10, 2),
(11, 2), (12, 2), (13, 2), (14, 2), (15, 2), (16, 2), (17, 2), (18, 2), (19, 2),
(20, 2), (21, 2), (22, 2), (23, 2), (24, 2);

INSERT INTO `sys_permission` (`id`, `parent_id`, `permission_code`, `permission_name`, `type`, `path`, `icon`, `sort_order`) VALUES
(1,  0, 'admin',            '管理后台',     1, '/admin',          'Setting',   0),
(2,  1, 'admin:user',       '用户管理',     1, '/admin/users',    'User',      1),
(3,  1, 'admin:order',      '订单仲裁',     1, '/admin/orders',   'Document',  2),
(4,  1, 'admin:credit',     '信用审核',     1, '/admin/credit',   'Medal',     3),
(5,  1, 'admin:campus',     '校园认证',     1, '/admin/campus',   'School',    4),
(6,  1, 'admin:system',     '系统配置',     1, '/admin/system',   'Tools',     5),
(7,  1, 'admin:stat',       '平台统计',     1, '/admin/stat',     'DataLine',  6),
(8,  2, 'admin:user:list',  '用户列表',     2, NULL, NULL, 1),
(9,  2, 'admin:user:edit',  '修改用户',     2, NULL, NULL, 2),
(10, 3, 'admin:order:list', '订单列表',     2, NULL, NULL, 1),
(11, 3, 'admin:order:arb',  '仲裁订单',     2, NULL, NULL, 2);

INSERT INTO `sys_role_permission` (`role_id`, `permission_id`) VALUES
(1, 1), (1, 2), (1, 3), (1, 4), (1, 5), (1, 6), (1, 7), (1, 8), (1, 9), (1, 10), (1, 11);

-- ==================== 4. 系统配置 ====================

INSERT INTO `sys_config` (`config_key`, `config_value`, `config_name`, `config_type`, `description`) VALUES
('site.name',                   '搭把手',  '站点名称',              'business', '前台展示的平台名称'),
('site.notice',                 '欢迎使用搭把手技能互助平台！新用户注册送100积分。', '站点公告', 'business', '首页公告'),
('order.auto_cancel_minutes',   '30',      '订单自动取消分钟数',    'business', '待支付订单自动取消时间'),
('order.confirm_timeout_hours', '72',      '确认超时时间',          'business', '服务核销后买家确认超时时间'),
('point.register_bonus',        '100',     '注册奖励积分',          'business', '新用户注册赠送积分'),
('point.sign_in_reward',        '5',       '签到奖励积分',          'business', '每日签到奖励'),
('point.review_bonus',          '10',      '评价奖励积分',          'business', '完成评价奖励积分'),
('credit.violation_penalty',    '0.5',     '违规默认扣分',          'business', '管理员处理违规时的默认扣分'),
('trust.newcomer_max',          '2.9',     '新人最高分',            'business', '新人等级上限'),
('trust.reliable_max',          '3.9',     '靠谱最高分',            'business', '靠谱等级上限');

-- ==================== 5. 用户技能 (63条) ====================

INSERT INTO `dbs_user_skill` (`user_id`, `skill_tag_id`, `proficiency`, `description`) VALUES
(2, 16, 4, 'Python全栈开发，3年经验'), (2, 17, 3, 'Java Spring Boot项目'), (2, 18, 3, 'Vue3+TypeScript前端'), (2, 19, 3, '微信小程序开发'),
(3, 11, 4, '海报设计，社团招新专业户'), (3, 12, 3, 'Logo/VI设计'), (3, 13, 3, 'Figma/Sketch UI设计'), (3, 14, 4, 'PPT美化，答辩必备'),
(4, 26, 4, '篮球校队后卫，三分超准'), (4, 27, 3, '健身教练证，减脂增肌'),
(5, 16, 4, 'Python竞赛金牌，LeetCode 2000+'), (5, 17, 3, 'Java项目经验丰富'), (5, 19, 3, '微信小程序开发'),
(6, 11, 3, '海报设计有创意'), (6, 15, 4, '专业摄影师，PS大师'), (6, 13, 3, 'Figma/Sketch都行'),
(7, 1, 4, '高数答疑，讲题清晰'), (7, 3, 3, '考研数学145分'), (7, 4, 2, '论文排版'),
(8, 26, 1, '想学篮球'), (8, 31, 1, '想学吉他'),
(9, 16, 4, '深度学习博士在读'), (9, 20, 3, '服务器部署'), (9, 17, 3, '分布式系统'),
(10, 31, 4, '吉他十级，可教指弹'), (10, 32, 3, '声乐辅导'),
(11, 26, 4, '校队后卫，三分超准'), (11, 27, 3, '健身教练证'),
(12, 2, 4, '专八优秀，可带口语'), (12, 4, 3, 'SCI论文润色'),
(13, 13, 4, 'UI设计，有大厂实习'), (13, 14, 3, 'PPT模板设计'),
(14, 16, 3, 'Python后端'), (14, 17, 4, 'Java架构师'), (14, 18, 4, 'React/Vue精通'),
(15, 1, 2, '基础化学辅导'),
(16, 4, 3, '论文写作指导'), (16, 2, 2, '英语六级580'),
(17, 1, 3, '大学物理辅导'), (17, 5, 2, 'Python数据分析'),
(18, 1, 3, '生物化学辅导'), (18, 27, 2, '运动康复知识'),
(19, 11, 2, '新闻海报设计'), (19, 14, 3, 'PPT演讲培训'),
(20, 1, 3, '高数/线代/概率论'), (20, 4, 3, '学术论文指导'),
(21, 6, 3, '电脑硬件维修'), (21, 19, 2, '嵌入式开发'),
(22, 4, 2, '文科论文写作'),
(23, 2, 3, '日语N1，可教基础'), (23, 31, 2, '声乐基础'),
(24, 11, 3, '建筑效果图设计'), (24, 15, 4, '手绘/板绘都行');

-- ==================== 6. 技能货架 (20个，显式ID) ====================

INSERT INTO `dbs_skill_shelf` (`id`, `user_id`, `skill_tag_id`, `title`, `description`, `point_price`, `duration_minutes`, `location_type`, `status`) VALUES
(1,  2,  16, 'Python编程辅导',        '从零基础到项目实战，含爬虫/Web/数据分析',   150, 60,  1, 1),
(2,  2,  18, 'Vue3前端开发辅导',       'Vue3+TypeScript+Pinia全栈教学',            180, 90,  1, 1),
(3,  3,  11, '海报/宣传图设计',        '社团招新/活动宣传/产品海报设计',            200, 120, 3, 1),
(4,  3,  14, 'PPT美化/答辩PPT',        '毕业答辩/项目汇报PPT设计美化',             120, 60,  1, 1),
(5,  4,  26, '篮球技术教学',           '投篮/运球/过人技巧，实战训练',             100, 60,  2, 1),
(6,  5,  16, 'Python算法辅导',         'ACM银牌选手教你刷题，从入门到进阶',        200, 90,  1, 1),
(7,  5,  19, '微信小程序开发',         '从零开始教你做小程序，含云开发',            180, 60,  1, 1),
(8,  6,  15, '专业摄影修图',           '人像/风景/产品摄影后期处理',               250, 120, 3, 1),
(9,  7,  1,  '高数期末冲刺',           '针对期末考试的重点讲解和刷题',             150, 90,  1, 1),
(10, 7,  3,  '考研数学辅导',           '数一/数二/数三全面辅导',                   300, 120, 1, 1),
(11, 9,  16, '深度学习入门',           'PyTorch实战，CNN/RNN/Transformer',          350, 120, 1, 1),
(12, 10, 31, '吉他零基础教学',         '从和弦开始，一个月能弹唱',                 120, 60,  2, 1),
(13, 11, 26, '篮球技术提升',           '投篮/运篮/过人技巧，实战训练',             100, 60,  2, 1),
(14, 12, 2,  '英语口语陪练',           '全英文对话，纠正发音和语法',               130, 60,  1, 1),
(15, 13, 13, 'APP界面设计',            '从原型到高保真，含设计规范',               280, 120, 1, 1),
(16, 14, 17, 'Java项目实战',           '带你从零搭建Spring Boot项目',              250, 120, 1, 1),
(17, 14, 18, 'React前端开发',          'Hooks+TypeScript实战项目',                  220, 90,  1, 1),
(18, 17, 1,  '大学物理答疑',           '力学/电磁学/光学，讲题耐心',               130, 60,  3, 1),
(19, 20, 1,  '概率论与数理统计',       '期末重点梳理，公式推导',                   160, 90,  1, 1),
(20, 24, 15, '手绘插画教学',           '素描/水彩/板绘，有作品集',                 200, 90,  2, 1);

ALTER TABLE `dbs_skill_shelf` AUTO_INCREMENT = 21;

-- ==================== 7. 需求 (16个，显式ID) ====================

INSERT INTO `dbs_demand` (`id`, `user_id`, `skill_tag_id`, `title`, `description`, `point_reward`, `deadline`, `location_type`, `campus`, `building`, `status`, `demand_type`) VALUES
(1,  8,  16, 'Python课设求助',        '大一Python课设不会写，求大佬带',             200, CONCAT(CURDATE() + INTERVAL 18 DAY, ' 23:59:59'), 1, '西校区', '1号宿舍楼',  1, 1),
(2,  8,  31, '想学吉他弹唱',          '零基础想学吉他，有没有耐心的老师',           150, CONCAT(CURDATE() + INTERVAL 30 DAY, ' 23:59:59'), 2, '西校区', '1号宿舍楼',  1, 1),
(3,  6,  17, 'Java Web大作业',        '需要一个完整的Web项目，有偿',                300, CONCAT(CURDATE() + INTERVAL 13 DAY, ' 23:59:59'), 1, '东校区', '5号宿舍楼',  1, 1),
(4,  11, 11, '社团招新海报设计',      '武术社需要一张中国风海报',                   120, CONCAT(CURDATE() + INTERVAL 6 DAY,  ' 23:59:59'), 1, '主校区', '8号宿舍楼',  1, 1),
(5,  15, 6,  '电脑开不了机',          '笔记本突然黑屏，急！',                      80, CONCAT(CURDATE() + INTERVAL 1 DAY,  ' 18:00:00'), 2, '主校区', '10号宿舍楼', 1, 1),
(6,  19, 16, '爬虫脚本编写',          '需要爬取一些公开数据，Python实现',           180, CONCAT(CURDATE() + INTERVAL 10 DAY, ' 23:59:59'), 1, '主校区', '12号宿舍楼', 1, 1),
(7,  22, 14, '毕业论文PPT',           '下周答辩，需要做一个精美的PPT',              150, CONCAT(CURDATE() + INTERVAL 3 DAY,  ' 23:59:59'), 1, '东校区', '5号宿舍楼',  1, 1),
(8,  18, 26, '篮球入门教学',          '完全不会打篮球，想学基础',                   100, CONCAT(CURDATE() + INTERVAL 18 DAY, ' 23:59:59'), 2, '东校区', '4号宿舍楼',  1, 1),
(9,  21, 13, '产品原型设计',          '有个创业项目想法，需要UI原型',               250, CONCAT(CURDATE() + INTERVAL 16 DAY, ' 23:59:59'), 1, '主校区', '13号宿舍楼', 1, 1),
(10, 7,  2,  '英语六级辅导',          '12月考六级，需要系统辅导',                   200, CONCAT(CURDATE() + INTERVAL 150 DAY,' 23:59:59'), 1, '主校区', '6号宿舍楼',  1, 1),
(11, 12, 18, 'Vue3项目重构',          '老项目需要升级到Vue3+TypeScript',            300, CONCAT(CURDATE() + INTERVAL 23 DAY, ' 23:59:59'), 1, '西校区', '2号宿舍楼',  1, 1),
(12, 23, 15, '产品宣传图设计',        '需要一套产品宣传图，5张左右',                350, CONCAT(CURDATE() + INTERVAL 13 DAY, ' 23:59:59'), 1, '主校区', '14号宿舍楼', 1, 1),
(13, 2,  2,  '英语口语陪练',          '想提高日常英语口语，找陪练',                 100, CONCAT(CURDATE() + INTERVAL 20 DAY, ' 23:59:59'), 1, '主校区', '1号宿舍楼',  1, 2),
(14, 3,  16, '数据分析脚本',          '需要Python数据分析脚本，处理Excel',          200, CONCAT(CURDATE() + INTERVAL 7 DAY,  ' 23:59:59'), 1, '东校区', '2号宿舍楼',  1, 1),
(15, 4,  11, '球队队服设计',          '篮球队需要一套队服设计方案',                 180, CONCAT(CURDATE() + INTERVAL 14 DAY, ' 23:59:59'), 1, '西校区', '3号宿舍楼',  1, 1),
(16, 9,  4,  '论文润色',              'SCI论文英语润色，3000词左右',                250, CONCAT(CURDATE() + INTERVAL 10 DAY, ' 23:59:59'), 1, '主校区', '7号宿舍楼',  1, 1);

ALTER TABLE `dbs_demand` AUTO_INCREMENT = 17;

-- ==================== 8. 订单 (14个，显式ID) ====================
-- 新流程: 1(待核销)→3(服务中)→5(已完成)
-- 核销码: buyer_verify_code/seller_verify_code (启动阶段), buyer_confirm_code/seller_confirm_code (完成阶段)

INSERT INTO `dbs_order` (`id`, `order_no`, `buyer_id`, `seller_id`, `demand_id`, `skill_shelf_id`, `skill_tag_id`, `title`, `point_amount`, `status`,
  `buyer_verify_code`, `seller_verify_code`, `buyer_confirm_code`, `seller_confirm_code`,
  `buyer_verified`, `seller_verified`, `buyer_confirmed`, `seller_confirmed`,
  `refund_requester`, `refund_agreed`, `remark`, `create_time`, `update_time`) VALUES
-- 已完成 (status=5): 双方核销+确认完毕
(1,  'DBS20260625001', 3,  2,  NULL, 1,  16, 'Python编程辅导',     150, 5, 'A1B2C3', 'D4E5F6', 'G7H8I9', 'J0K1L2', 1, 1, 1, 1, NULL, 0, '老师讲得很好',     CONCAT(CURDATE() - INTERVAL 7 DAY, ' 10:00:00'), CONCAT(CURDATE() - INTERVAL 6 DAY, ' 16:00:00')),
(2,  'DBS20260626001', 4,  3,  NULL, 3,  11, '海报设计',           180, 5, 'M3N4O5', 'P6Q7R8', 'S9T0U1', 'V2W3X4', 1, 1, 1, 1, NULL, 0, '设计效果超预期',   CONCAT(CURDATE() - INTERVAL 6 DAY, ' 09:00:00'), CONCAT(CURDATE() - INTERVAL 5 DAY, ' 15:00:00')),
(3,  'DBS20260627001', 2,  4,  NULL, 5,  26, '篮球陪练',           120, 5, 'Y5Z6A7', 'B8C9D0', 'E1F2G3', 'H4I5J6', 1, 1, 1, 1, NULL, 0, '技术很好',         CONCAT(CURDATE() - INTERVAL 5 DAY, ' 14:00:00'), CONCAT(CURDATE() - INTERVAL 4 DAY, ' 20:00:00')),
(4,  'DBS20260628001', 8,  7,  1,   NULL, 1,  '高数期末冲刺',       150, 5, 'K7L8M9', 'N0O1P2', 'Q3R4S5', 'T6U7V8', 1, 1, 1, 1, NULL, 0, '讲得很清楚',       CONCAT(CURDATE() - INTERVAL 4 DAY, ' 08:00:00'), CONCAT(CURDATE() - INTERVAL 3 DAY, ' 17:00:00')),
(5,  'DBS20260628002', 6,  5,  NULL, 1,  16, 'Python算法辅导',     200, 5, 'W9X0Y1', 'Z2A3B4', 'C5D6E7', 'F8G9H0', 1, 1, 1, 1, NULL, 0, '学到很多',         CONCAT(CURDATE() - INTERVAL 4 DAY, ' 10:00:00'), CONCAT(CURDATE() - INTERVAL 3 DAY, ' 19:00:00')),
-- 服务中 (status=3): 双方已核销，等待确认
(6,  'DBS20260629001', 8,  10, NULL, 12, 31, '吉他零基础教学',     120, 3, 'I1J2K3', 'L4M5N6', 'O7P8Q9', 'R0S1T2', 1, 1, 0, 0, NULL, 0, NULL,              CONCAT(CURDATE() - INTERVAL 3 DAY, ' 09:00:00'), CONCAT(CURDATE() - INTERVAL 2 DAY, ' 09:00:00')),
(7,  'DBS20260629002', 15, 12, NULL, 14, 2,  '英语口语陪练',       130, 3, 'U3V4W5', 'X6Y7Z8', 'A9B0C1', 'D2E3F4', 1, 1, 0, 0, NULL, 0, NULL,              CONCAT(CURDATE() - INTERVAL 3 DAY, ' 14:00:00'), CONCAT(CURDATE() - INTERVAL 2 DAY, ' 14:00:00')),
(8,  'DBS20260630001', 22, 14, NULL, 16, 17, 'Java项目实战',       250, 3, 'G5H6I7', 'J8K9L0', 'M1N2O3', 'P4Q5R6', 1, 1, 0, 0, NULL, 0, NULL,              CONCAT(CURDATE() - INTERVAL 2 DAY, ' 08:00:00'), CONCAT(CURDATE() - INTERVAL 1 DAY, ' 08:00:00')),
-- 服务中 (status=3): 一方已确认，等待另一方
(9,  'DBS20260630002', 11, 5,  NULL, 7,  19, '微信小程序开发',     180, 3, 'S7T8U9', 'V0W1X2', 'Y3Z4A5', 'B6C7D8', 1, 1, 1, 0, NULL, 0, '已学完',           CONCAT(CURDATE() - INTERVAL 4 DAY, ' 10:00:00'), CONCAT(CURDATE() - INTERVAL 1 DAY, ' 10:00:00')),
(10, 'DBS20260630003', 19, 13, NULL, 15, 13, 'APP界面设计',        280, 3, 'E9F0G1', 'H2I3J4', 'K5L6M7', 'N8O9P0', 1, 1, 0, 1, NULL, 0, '设计完成',         CONCAT(CURDATE() - INTERVAL 3 DAY, ' 09:00:00'), CONCAT(CURDATE() - INTERVAL 1 DAY, ' 11:00:00')),
-- 待核销 (status=1): 等待双方输入核销码
(11, 'DBS20260630004', 7,  9,  NULL, 11, 16, '深度学习入门',       350, 1, 'Q1R2S3', 'T4U5V6', NULL, NULL, 0, 0, 0, 0, NULL, 0, NULL,              CONCAT(CURDATE() - INTERVAL 2 DAY, ' 12:00:00'), CONCAT(CURDATE() - INTERVAL 2 DAY, ' 12:00:00')),
(12, 'DBS20260630005', 18, 11, NULL, 13, 26, '篮球技术提升',       100, 1, 'W7X8Y9', 'Z0A1B2', NULL, NULL, 0, 0, 0, 0, NULL, 0, NULL,              CONCAT(CURDATE() - INTERVAL 2 DAY, ' 13:00:00'), CONCAT(CURDATE() - INTERVAL 2 DAY, ' 13:00:00')),
-- 待核销 (status=1): 一方已核销，等待另一方
(13, 'DBS20260630006', 21, 17, NULL, 9,  1,  '大学物理答疑',       130, 1, 'C3D4E5', 'F6G7H8', NULL, NULL, 1, 0, 0, 0, NULL, 0, NULL,              CONCAT(CURDATE() - INTERVAL 1 DAY, ' 10:00:00'), CONCAT(CURDATE() - INTERVAL 1 DAY, ' 10:30:00')),
-- 已取消 (status=0)
(14, 'DBS20260625002', 2,  3,  NULL, 4,  14, 'PPT美化',            100, 0, 'I9J0K1', 'L2M3N4', NULL, NULL, 0, 0, 0, 0, NULL, 0, '临时有事取消',     CONCAT(CURDATE() - INTERVAL 7 DAY, ' 15:00:00'), CONCAT(CURDATE() - INTERVAL 7 DAY, ' 16:00:00')),
-- 争议中 (status=7)
(15, 'DBS20260629003', 4,  2,  NULL, 2,  18, '前端页面开发',       200, 7, 'O5P6Q7', 'R8S9T0', 'U1V2W3', 'X4Y5Z6', 1, 1, 1, 1, NULL, 0, '效果不满意',       CONCAT(CURDATE() - INTERVAL 3 DAY, ' 10:00:00'), CONCAT(CURDATE() - INTERVAL 2 DAY, ' 09:00:00')),
-- 服务中 (status=3): 退款申请中
(16, 'DBS20260630007', 23, 14, NULL, 21, 18, 'React前端开发',      220, 3, 'A7B8C9', 'D0E1F2', 'G3H4I5', 'J6K7L8', 1, 1, 0, 0, 'buyer', 0, NULL, CONCAT(CURDATE() - INTERVAL 2 DAY, ' 14:00:00'), CONCAT(CURDATE() - INTERVAL 1 DAY, ' 16:00:00'));

ALTER TABLE `dbs_order` AUTO_INCREMENT = 17;

-- ==================== 9. 积分账户 (24个) ====================

INSERT INTO `dbs_point_account` (`user_id`, `available`, `frozen`, `total_earned`, `total_spent`) VALUES
(1,  99999, 0,    99999, 0),
(2,  650,   200,  800,   350),
(3,  480,   0,    630,   330),
(4,  180,   0,    300,   240),
(5,  520,   350,  750,   200),
(6,  130,   0,    280,   300),
(7,  300,   0,    450,   300),
(8,  60,    120,  150,   210),
(9,  950,   250,  1200,  0),
(10, 500,   0,    620,   240),
(11, 100,   180,  200,   220),
(12, 430,   130,  560,   0),
(13, 400,   280,  680,   0),
(14, 850,   250,  1100,  0),
(15, 50,    130,  130,   180),
(16, 200,   0,    350,   230),
(17, 450,   0,    580,   260),
(18, 300,   0,    400,   200),
(19, 100,   280,  250,   330),
(20, 500,   0,    700,   350),
(21, 380,   0,    480,   200),
(22, 50,    250,  150,   200),
(23, 380,   220,  530,   300),
(24, 520,   200,  720,   0);

-- ==================== 10. 积分流水 ====================

INSERT INTO `dbs_point_transaction` (`user_id`, `order_id`, `type`, `amount`, `balance_after`, `description`, `create_time`) VALUES
(2,  NULL, 5, 100, 100, '新用户注册奖励', CONCAT(CURDATE() - INTERVAL 12 DAY, ' 10:00:00')),
(3,  NULL, 5, 100, 100, '新用户注册奖励', CONCAT(CURDATE() - INTERVAL 12 DAY, ' 10:00:00')),
(4,  NULL, 5, 100, 100, '新用户注册奖励', CONCAT(CURDATE() - INTERVAL 12 DAY, ' 10:00:00')),
(5,  NULL, 5, 100, 100, '新用户注册奖励', CONCAT(CURDATE() - INTERVAL 12 DAY, ' 10:00:00')),
(6,  NULL, 5, 100, 100, '新用户注册奖励', CONCAT(CURDATE() - INTERVAL 11 DAY, ' 10:00:00')),
(7,  NULL, 5, 100, 100, '新用户注册奖励', CONCAT(CURDATE() - INTERVAL 11 DAY, ' 10:00:00')),
(8,  NULL, 5, 100, 100, '新用户注册奖励', CONCAT(CURDATE() - INTERVAL 11 DAY, ' 10:00:00')),
(9,  NULL, 5, 100, 100, '新用户注册奖励', CONCAT(CURDATE() - INTERVAL 10 DAY, ' 10:00:00')),
(10, NULL, 5, 100, 100, '新用户注册奖励', CONCAT(CURDATE() - INTERVAL 10 DAY, ' 10:00:00')),
(2,  1,  1, 150, 250,  '订单DBS20260625001收入', CONCAT(CURDATE() - INTERVAL 7 DAY, ' 16:00:00')),
(3,  2,  1, 180, 280,  '订单DBS20260626001收入', CONCAT(CURDATE() - INTERVAL 6 DAY, ' 15:00:00')),
(4,  3,  1, 120, 220,  '订单DBS20260627001收入', CONCAT(CURDATE() - INTERVAL 5 DAY, ' 20:00:00')),
(7,  4,  1, 150, 350,  '订单DBS20260628001收入', CONCAT(CURDATE() - INTERVAL 4 DAY, ' 17:00:00')),
(5,  5,  1, 200, 400,  '订单DBS20260628002收入', CONCAT(CURDATE() - INTERVAL 4 DAY, ' 19:00:00')),
(3,  1,  2, 150, 130,  '订单DBS20260625001支出', CONCAT(CURDATE() - INTERVAL 7 DAY, ' 16:00:00')),
(4,  2,  2, 180, 40,   '订单DBS20260626001支出', CONCAT(CURDATE() - INTERVAL 6 DAY, ' 15:00:00')),
(2,  3,  2, 120, 130,  '订单DBS20260627001支出', CONCAT(CURDATE() - INTERVAL 5 DAY, ' 20:00:00')),
(8,  6,  3, 120, 60,   '订单DBS20260629001冻结', CONCAT(CURDATE() - INTERVAL 3 DAY, ' 09:00:00')),
(15, 7,  3, 130, 50,   '订单DBS20260629002冻结', CONCAT(CURDATE() - INTERVAL 3 DAY, ' 14:00:00')),
(22, 8,  3, 250, 50,   '订单DBS20260630001冻结', CONCAT(CURDATE() - INTERVAL 2 DAY, ' 08:00:00')),
(11, 9,  3, 180, 100,  '订单DBS20260630002冻结', CONCAT(CURDATE() - INTERVAL 4 DAY, ' 10:00:00')),
(19, 10, 3, 280, 100,  '订单DBS20260630003冻结', CONCAT(CURDATE() - INTERVAL 3 DAY, ' 09:00:00')),
(23, 16, 3, 220, 380,  '订单DBS20260630007冻结', CONCAT(CURDATE() - INTERVAL 2 DAY, ' 14:00:00'));

-- ==================== 11. 积分冻结记录 ====================

INSERT INTO `dbs_point_freeze` (`order_id`, `user_id`, `amount`, `status`, `freeze_time`, `release_time`) VALUES
(6,  8,  120, 1, CONCAT(CURDATE() - INTERVAL 3 DAY, ' 09:00:00'), NULL),
(7,  15, 130, 1, CONCAT(CURDATE() - INTERVAL 3 DAY, ' 14:00:00'), NULL),
(8,  22, 250, 1, CONCAT(CURDATE() - INTERVAL 2 DAY, ' 08:00:00'), NULL),
(9,  11, 180, 1, CONCAT(CURDATE() - INTERVAL 4 DAY, ' 10:00:00'), NULL),
(10, 19, 280, 1, CONCAT(CURDATE() - INTERVAL 3 DAY, ' 09:00:00'), NULL),
(16, 23, 220, 1, CONCAT(CURDATE() - INTERVAL 2 DAY, ' 14:00:00'), NULL);

-- ==================== 12. 担保池 ====================

INSERT INTO `dbs_guarantee_pool` (`order_id`, `amount`, `status`, `settle_time`) VALUES
(1, 150, 2, CONCAT(CURDATE() - INTERVAL 7 DAY, ' 16:00:00')),
(2, 180, 2, CONCAT(CURDATE() - INTERVAL 6 DAY, ' 15:00:00')),
(3, 120, 2, CONCAT(CURDATE() - INTERVAL 5 DAY, ' 20:00:00')),
(6, 120, 1, NULL),
(7, 130, 1, NULL),
(8, 250, 1, NULL),
(9, 180, 1, NULL),
(10, 280, 1, NULL),
(16, 220, 1, NULL);

-- ==================== 13. 评价 ====================

INSERT INTO `dbs_review` (`order_id`, `reviewer_id`, `reviewee_id`, `rating`, `content`, `is_anonymous`, `create_time`) VALUES
(1, 3,  2,  5, '张三大佬讲Python真的很清楚，一个小时就把我困扰几天的bug解决了，强烈推荐！', 0, CONCAT(CURDATE() - INTERVAL 7 DAY, ' 17:00:00')),
(1, 2,  3,  5, '李四同学很认真，学东西很快，是个好学生', 0, CONCAT(CURDATE() - INTERVAL 7 DAY, ' 17:30:00')),
(2, 4,  3,  5, '海报设计得太好了，超出预期，社团招新效果特别好', 0, CONCAT(CURDATE() - INTERVAL 6 DAY, ' 16:00:00')),
(2, 3,  4,  4, '需求沟通很顺畅，给的素材也很齐全', 0, CONCAT(CURDATE() - INTERVAL 6 DAY, ' 16:30:00')),
(3, 2,  4,  4, '王五篮球技术确实好，教得很耐心，下次还约', 0, CONCAT(CURDATE() - INTERVAL 5 DAY, ' 21:00:00')),
(3, 4,  2,  5, '张三学东西很认真，进步很快', 0, CONCAT(CURDATE() - INTERVAL 5 DAY, ' 21:30:00')),
(4, 8,  7,  5, '孙九学长讲高数太棒了，终于搞懂了极限和导数', 0, CONCAT(CURDATE() - INTERVAL 4 DAY, ' 18:00:00')),
(5, 6,  5,  5, '陈七的Python算法课含金量很高，ACM银牌名不虚传', 0, CONCAT(CURDATE() - INTERVAL 4 DAY, ' 20:00:00'));

-- ==================== 14. 聊天会话与消息 ====================

INSERT INTO `dbs_chat_session` (`id`, `user1_id`, `user2_id`, `last_message`, `last_time`, `unread_count`, `create_time`, `update_time`) VALUES
(1, 2,  3,  '好的，那我们明天下午2点开始', CONCAT(CURDATE() - INTERVAL 7 DAY, ' 15:00:00'), 0, CONCAT(CURDATE() - INTERVAL 7 DAY, ' 09:00:00'), CONCAT(CURDATE() - INTERVAL 7 DAY, ' 15:00:00')),
(2, 2,  4,  '篮球约起来！',                 CONCAT(CURDATE() - INTERVAL 5 DAY, ' 20:00:00'), 1, CONCAT(CURDATE() - INTERVAL 5 DAY, ' 10:00:00'), CONCAT(CURDATE() - INTERVAL 5 DAY, ' 20:00:00')),
(3, 5,  6,  'Python项目做完了吗？',         CONCAT(CURDATE() - INTERVAL 4 DAY, ' 19:00:00'), 2, CONCAT(CURDATE() - INTERVAL 4 DAY, ' 09:00:00'), CONCAT(CURDATE() - INTERVAL 4 DAY, ' 19:00:00')),
(4, 7,  8,  '高数还有不懂的可以问我',       CONCAT(CURDATE() - INTERVAL 4 DAY, ' 17:00:00'), 0, CONCAT(CURDATE() - INTERVAL 4 DAY, ' 16:00:00'), CONCAT(CURDATE() - INTERVAL 4 DAY, ' 17:00:00')),
(5, 10, 8,  '吉他调好弦了吗？',             CONCAT(CURDATE() - INTERVAL 3 DAY, ' 09:00:00'), 1, CONCAT(CURDATE() - INTERVAL 3 DAY, ' 08:00:00'), CONCAT(CURDATE() - INTERVAL 3 DAY, ' 09:00:00')),
(6, 12, 15, 'See you tomorrow!',            CONCAT(CURDATE() - INTERVAL 3 DAY, ' 14:00:00'), 0, CONCAT(CURDATE() - INTERVAL 3 DAY, ' 13:00:00'), CONCAT(CURDATE() - INTERVAL 3 DAY, ' 14:00:00')),
(7, 13, 19, '原型图发你看看',               CONCAT(CURDATE() - INTERVAL 2 DAY, ' 11:00:00'), 1, CONCAT(CURDATE() - INTERVAL 3 DAY, ' 08:00:00'), CONCAT(CURDATE() - INTERVAL 2 DAY, ' 11:00:00')),
(8, 14, 22, 'Spring Boot环境配好了吗？',    CONCAT(CURDATE() - INTERVAL 2 DAY, ' 08:00:00'), 0, CONCAT(CURDATE() - INTERVAL 2 DAY, ' 07:00:00'), CONCAT(CURDATE() - INTERVAL 2 DAY, ' 08:00:00'));

INSERT INTO `dbs_chat_message` (`session_id`, `sender_id`, `content`, `msg_type`, `is_read`, `create_time`) VALUES
(1, 2, '你好，我看到你需要Python辅导？',         1, 1, CONCAT(CURDATE() - INTERVAL 7 DAY, ' 09:00:00')),
(1, 3, '是的，我有个爬虫项目不会写',             1, 1, CONCAT(CURDATE() - INTERVAL 7 DAY, ' 09:05:00')),
(1, 2, '没问题，我可以教你，明天下午有空吗？',   1, 1, CONCAT(CURDATE() - INTERVAL 7 DAY, ' 09:10:00')),
(1, 3, '有空的，几点合适？',                     1, 1, CONCAT(CURDATE() - INTERVAL 7 DAY, ' 09:15:00')),
(1, 2, '好的，那我们明天下午2点开始',            1, 1, CONCAT(CURDATE() - INTERVAL 7 DAY, ' 15:00:00')),
(2, 4, '三哥，周末打篮球不？',                   1, 1, CONCAT(CURDATE() - INTERVAL 5 DAY, ' 10:00:00')),
(2, 2, '好啊，几点？',                           1, 1, CONCAT(CURDATE() - INTERVAL 5 DAY, ' 10:30:00')),
(2, 4, '下午3点，东校区篮球场',                  1, 1, CONCAT(CURDATE() - INTERVAL 5 DAY, ' 11:00:00')),
(2, 2, '篮球约起来！',                           1, 0, CONCAT(CURDATE() - INTERVAL 5 DAY, ' 20:00:00')),
(3, 5, '赵八，Python项目做完了吗？',             1, 1, CONCAT(CURDATE() - INTERVAL 4 DAY, ' 09:00:00')),
(3, 6, '快了，还有个bug没修好',                  1, 1, CONCAT(CURDATE() - INTERVAL 4 DAY, ' 10:00:00')),
(3, 5, '需要帮忙吗？',                           1, 1, CONCAT(CURDATE() - INTERVAL 4 DAY, ' 11:00:00')),
(3, 6, '好的，晚上可以帮我看看吗？',             1, 0, CONCAT(CURDATE() - INTERVAL 4 DAY, ' 18:00:00')),
(3, 5, 'Python项目做完了吗？',                   1, 0, CONCAT(CURDATE() - INTERVAL 4 DAY, ' 19:00:00')),
(5, 10, '周十，吉他调好弦了吗？',                1, 0, CONCAT(CURDATE() - INTERVAL 3 DAY, ' 08:00:00')),
(5, 8,  '调好了，但是E弦好像有点松',             1, 1, CONCAT(CURDATE() - INTERVAL 3 DAY, ' 08:30:00')),
(5, 10, '没关系，明天上课我帮你调',              1, 0, CONCAT(CURDATE() - INTERVAL 3 DAY, ' 09:00:00'));

-- ==================== 15. 通知 ====================

INSERT INTO `dbs_notification` (`user_id`, `type`, `title`, `content`, `related_type`, `related_id`, `is_read`, `create_time`) VALUES
(2,  'order',  '订单完成',     '您的订单DBS20260625001已完成，积分已到账', 'order',  1,  1, CONCAT(CURDATE() - INTERVAL 7 DAY, ' 16:00:00')),
(3,  'order',  '订单完成',     '您的订单DBS20260626001已完成，请评价',     'order',  2,  1, CONCAT(CURDATE() - INTERVAL 6 DAY, ' 15:00:00')),
(4,  'order',  '订单完成',     '您的订单DBS20260627001已完成',             'order',  3,  1, CONCAT(CURDATE() - INTERVAL 5 DAY, ' 20:00:00')),
(8,  'order',  '新订单',       '您有一个新订单DBS20260629001待服务',       'order',  6,  0, CONCAT(CURDATE() - INTERVAL 3 DAY, ' 09:00:00')),
(15, 'order',  '新订单',       '您有一个新订单DBS20260629002待服务',       'order',  7,  0, CONCAT(CURDATE() - INTERVAL 3 DAY, ' 14:00:00')),
(2,  'credit', '收到评价',     '李四给您了5星好评',                         'review', 1,  1, CONCAT(CURDATE() - INTERVAL 7 DAY, ' 17:00:00')),
(3,  'credit', '收到评价',     '张三给您了5星好评',                         'review', 2,  1, CONCAT(CURDATE() - INTERVAL 7 DAY, ' 17:30:00')),
(7,  'point',  '积分到账',     '订单DBS20260628001收入150积分',            NULL,     NULL, 1, CONCAT(CURDATE() - INTERVAL 4 DAY, ' 17:00:00')),
(5,  'point',  '积分到账',     '订单DBS20260628002收入200积分',            NULL,     NULL, 1, CONCAT(CURDATE() - INTERVAL 4 DAY, ' 19:00:00')),
(8,  'system', '欢迎加入搭把手', '感谢注册搭把手平台，送您100积分新手礼包！', NULL,  NULL, 1, CONCAT(CURDATE() - INTERVAL 12 DAY, ' 10:00:00')),
(22, 'order',  '订单争议',     '订单DBS20260629003存在争议，请处理',       'order',  15, 0, CONCAT(CURDATE() - INTERVAL 2 DAY, ' 09:00:00')),
(11, 'order',  '待核销',       '订单DBS20260630004等待您核销启动服务',     'order',  11, 0, CONCAT(CURDATE() - INTERVAL 2 DAY, ' 10:00:00'));

-- ==================== 16. 违规与申诉 ====================

INSERT INTO `credit_violation` (`user_id`, `order_id`, `type`, `description`, `penalty_score`, `reporter_id`, `status`, `create_time`) VALUES
(22, 15, 'other', '订单完成后恶意差评', 0.5, 2, 1, CONCAT(CURDATE() - INTERVAL 2 DAY, ' 10:00:00'));

INSERT INTO `credit_appeal` (`violation_id`, `appellant_id`, `reason`, `status`, `create_time`) VALUES
(1, 22, '我认为评价是客观的，不存在恶意差评', 0, CONCAT(CURDATE() - INTERVAL 2 DAY, ' 10:30:00'));

-- ==================== 17. 校园认证 ====================

INSERT INTO `user_campus_auth` (`user_id`, `auth_type`, `student_no`, `real_name`, `campus`, `college`, `status`, `reviewer_id`, `review_time`, `create_time`) VALUES
(2,  'student', '2022001001', '张三丰', '主校区', '计算机科学与技术学院', 1, 1, CONCAT(CURDATE() - INTERVAL 10 DAY, ' 14:00:00'), CONCAT(CURDATE() - INTERVAL 12 DAY, ' 10:00:00')),
(3,  'student', '2022002002', '李思思', '东校区', '艺术设计学院',         1, 1, CONCAT(CURDATE() - INTERVAL 9 DAY,  ' 15:00:00'), CONCAT(CURDATE() - INTERVAL 11 DAY, ' 09:00:00')),
(5,  'student', '2023001005', '陈七七', '主校区', '计算机科学与技术学院', 1, 1, CONCAT(CURDATE() - INTERVAL 8 DAY,  ' 16:00:00'), CONCAT(CURDATE() - INTERVAL 10 DAY, ' 11:00:00')),
(12, 'student', '2021003012', '林五五', '西校区', '外国语学院',           0, NULL, NULL, CONCAT(CURDATE() - INTERVAL 2 DAY, ' 09:00:00')),
(20, 'student', '2022004020', '冯一一', '西校区', '经济管理学院',         1, 1, CONCAT(CURDATE() - INTERVAL 5 DAY,  ' 10:00:00'), CONCAT(CURDATE() - INTERVAL 7 DAY, ' 14:00:00'));

-- ==================== 18. 信任分变动记录 ====================

INSERT INTO `user_trust_score_log` (`user_id`, `order_id`, `type`, `score_change`, `score_before`, `score_after`, `reason`, `create_time`) VALUES
(2,  1,  'order_complete',   0.3, 4.2, 4.5, '订单DBS20260625001完成',     CONCAT(CURDATE() - INTERVAL 7 DAY, ' 16:00:00')),
(3,  2,  'order_complete',   0.3, 4.5, 4.8, '订单DBS20260626001完成',     CONCAT(CURDATE() - INTERVAL 6 DAY, ' 15:00:00')),
(4,  3,  'order_complete',   0.2, 4.0, 4.2, '订单DBS20260627001完成',     CONCAT(CURDATE() - INTERVAL 5 DAY, ' 20:00:00')),
(7,  4,  'order_complete',   0.2, 4.0, 4.2, '订单DBS20260628001完成',     CONCAT(CURDATE() - INTERVAL 4 DAY, ' 17:00:00')),
(5,  5,  'order_complete',   0.3, 4.5, 4.8, '订单DBS20260628002完成',     CONCAT(CURDATE() - INTERVAL 4 DAY, ' 19:00:00')),
(22, NULL, 'violation',      -0.5, 3.0, 2.5, '违规：订单完成后恶意差评',  CONCAT(CURDATE() - INTERVAL 2 DAY, ' 10:00:00')),
(2,  NULL, 'review_bonus',    0.1, 4.4, 4.5, '完成评价奖励',               CONCAT(CURDATE() - INTERVAL 7 DAY, ' 17:00:00')),
(3,  NULL, 'review_bonus',    0.1, 4.7, 4.8, '完成评价奖励',               CONCAT(CURDATE() - INTERVAL 6 DAY, ' 16:30:00'));

-- ==================== 19. 签到记录 (最近7天) ====================

INSERT INTO `dbs_point_sign_in` (`user_id`, `sign_date`, `reward`, `consecutive_days`, `create_time`) VALUES
(2,  CURDATE() - INTERVAL 6 DAY, 5, 1, CONCAT(CURDATE() - INTERVAL 6 DAY, ' 08:00:00')),
(2,  CURDATE() - INTERVAL 5 DAY, 5, 2, CONCAT(CURDATE() - INTERVAL 5 DAY, ' 08:30:00')),
(2,  CURDATE() - INTERVAL 4 DAY, 5, 3, CONCAT(CURDATE() - INTERVAL 4 DAY, ' 07:50:00')),
(2,  CURDATE() - INTERVAL 3 DAY, 5, 4, CONCAT(CURDATE() - INTERVAL 3 DAY, ' 08:10:00')),
(2,  CURDATE() - INTERVAL 2 DAY, 5, 5, CONCAT(CURDATE() - INTERVAL 2 DAY, ' 09:00:00')),
(3,  CURDATE() - INTERVAL 5 DAY, 5, 1, CONCAT(CURDATE() - INTERVAL 5 DAY, ' 09:00:00')),
(3,  CURDATE() - INTERVAL 4 DAY, 5, 2, CONCAT(CURDATE() - INTERVAL 4 DAY, ' 08:45:00')),
(3,  CURDATE() - INTERVAL 3 DAY, 5, 3, CONCAT(CURDATE() - INTERVAL 3 DAY, ' 09:15:00')),
(5,  CURDATE() - INTERVAL 4 DAY, 5, 1, CONCAT(CURDATE() - INTERVAL 4 DAY, ' 07:30:00')),
(5,  CURDATE() - INTERVAL 3 DAY, 5, 2, CONCAT(CURDATE() - INTERVAL 3 DAY, ' 07:45:00')),
(5,  CURDATE() - INTERVAL 2 DAY, 5, 3, CONCAT(CURDATE() - INTERVAL 2 DAY, ' 08:00:00')),
(5,  CURDATE() - INTERVAL 1 DAY, 5, 4, CONCAT(CURDATE() - INTERVAL 1 DAY, ' 07:55:00')),
(7,  CURDATE() - INTERVAL 3 DAY, 5, 1, CONCAT(CURDATE() - INTERVAL 3 DAY, ' 10:00:00')),
(9,  CURDATE() - INTERVAL 2 DAY, 5, 1, CONCAT(CURDATE() - INTERVAL 2 DAY, ' 08:30:00')),
(9,  CURDATE() - INTERVAL 1 DAY, 5, 2, CONCAT(CURDATE() - INTERVAL 1 DAY, ' 08:15:00')),
(12, CURDATE() - INTERVAL 1 DAY, 5, 1, CONCAT(CURDATE() - INTERVAL 1 DAY, ' 09:30:00')),
(14, CURDATE() - INTERVAL 1 DAY, 5, 1, CONCAT(CURDATE() - INTERVAL 1 DAY, ' 07:00:00')),
(20, CURDATE() - INTERVAL 1 DAY, 5, 1, CONCAT(CURDATE() - INTERVAL 1 DAY, ' 08:45:00'));

-- ==================== 20. 时间格子 ====================

INSERT INTO `dbs_time_slot` (`user_id`, `date`, `start_time`, `end_time`, `status`) VALUES
(2, CURDATE(),         '09:00:00', '11:00:00', 1),
(2, CURDATE(),         '14:00:00', '16:00:00', 1),
(2, CURDATE() + INTERVAL 1 DAY, '09:00:00', '12:00:00', 1),
(2, CURDATE() + INTERVAL 2 DAY, '14:00:00', '17:00:00', 2),
(3, CURDATE(),         '10:00:00', '12:00:00', 1),
(3, CURDATE(),         '15:00:00', '17:00:00', 1),
(3, CURDATE() + INTERVAL 1 DAY, '09:00:00', '11:00:00', 1),
(5, CURDATE(),         '09:00:00', '12:00:00', 1),
(5, CURDATE() + INTERVAL 1 DAY, '14:00:00', '18:00:00', 1),
(10, CURDATE(),        '10:00:00', '12:00:00', 2),
(10, CURDATE() + INTERVAL 1 DAY, '14:00:00', '16:00:00', 1),
(14, CURDATE(),        '09:00:00', '12:00:00', 1),
(14, CURDATE(),        '14:00:00', '18:00:00', 2),
(14, CURDATE() + INTERVAL 1 DAY, '09:00:00', '18:00:00', 1);

-- ==================== 21. 每日统计 (最近11天) ====================

INSERT INTO `stat_daily_summary` (`stat_date`, `new_user_count`, `active_user_count`, `new_demand_count`, `new_order_count`, `completed_order_count`, `cancelled_order_count`, `point_inflow`, `point_outflow`) VALUES
(CURDATE() - INTERVAL 10 DAY, 4,  4,  0, 0, 0, 0, 400, 0),
(CURDATE() - INTERVAL 9 DAY,  2,  6,  0, 0, 0, 0, 200, 0),
(CURDATE() - INTERVAL 8 DAY,  3,  8,  1, 0, 0, 0, 300, 0),
(CURDATE() - INTERVAL 7 DAY,  2, 10,  1, 2, 1, 0, 350, 270),
(CURDATE() - INTERVAL 6 DAY,  1, 12,  1, 1, 1, 0, 280, 180),
(CURDATE() - INTERVAL 5 DAY,  2, 15,  2, 1, 1, 1, 220, 120),
(CURDATE() - INTERVAL 4 DAY,  1, 18,  1, 2, 2, 0, 450, 270),
(CURDATE() - INTERVAL 3 DAY,  2, 20,  2, 3, 0, 0, 150, 250),
(CURDATE() - INTERVAL 2 DAY,  1, 22,  3, 4, 0, 0, 200, 350),
(CURDATE() - INTERVAL 1 DAY,  0, 20,  1, 0, 0, 0, 50,  0),
(CURDATE(),                   0, 18,  0, 0, 0, 0, 0,   0);

-- ==================== 22. 技能热度统计 ====================

INSERT INTO `stat_skill_heat` (`stat_date`, `skill_tag_id`, `category_id`, `shelf_count`, `demand_count`, `order_count`, `heat_score`) VALUES
(CURDATE(), 16, 4, 3, 2, 3, 95.5),
(CURDATE(), 17, 4, 2, 2, 2, 88.0),
(CURDATE(), 11, 3, 2, 2, 2, 85.5),
(CURDATE(), 26, 5, 2, 1, 2, 82.0),
(CURDATE(), 2,  1, 1, 2, 1, 78.5),
(CURDATE(), 18, 4, 2, 1, 1, 75.0),
(CURDATE(), 31, 6, 1, 1, 1, 72.0),
(CURDATE(), 13, 3, 1, 1, 1, 70.0),
(CURDATE(), 1,  1, 2, 1, 1, 68.5),
(CURDATE(), 15, 3, 1, 1, 0, 65.0),
(CURDATE(), 14, 3, 1, 1, 1, 62.0),
(CURDATE(), 27, 5, 1, 1, 0, 58.0),
(CURDATE(), 6,  2, 1, 1, 0, 55.0),
(CURDATE(), 19, 4, 1, 0, 1, 52.0),
(CURDATE(), 4,  1, 1, 1, 0, 50.0);

-- ==================== 23. 操作日志示例 ====================

INSERT INTO `sys_log` (`operator_id`, `operation_type`, `operation_content`, `method`, `request_url`, `ip`, `status`, `cost_time_ms`, `create_time`) VALUES
(1, 'LOGIN',     '管理员登录',                'POST', '/api/v1/auth/login',     '127.0.0.1', 1, 45,  CONCAT(CURDATE() - INTERVAL 2 DAY, ' 09:00:00')),
(1, 'USER_EDIT', '修改用户状态: user_id=22',  'PUT',  '/api/admin/v1/users/22/status', '127.0.0.1', 1, 32, CONCAT(CURDATE() - INTERVAL 2 DAY, ' 10:05:00')),
(1, 'VIOLATION', '处理违规: violation_id=1',  'POST', '/api/admin/v1/violations/1', '127.0.0.1', 1, 28, CONCAT(CURDATE() - INTERVAL 2 DAY, ' 10:10:00')),
(1, 'LOGIN',     '管理员登录',                'POST', '/api/v1/auth/login',     '127.0.0.1', 1, 38,  CONCAT(CURDATE() - INTERVAL 1 DAY, ' 09:00:00')),
(1, 'CONFIG',    '更新系统配置',              'PUT',  '/api/admin/v1/config',   '127.0.0.1', 1, 22,  CONCAT(CURDATE() - INTERVAL 1 DAY, ' 14:00:00'));

SET FOREIGN_KEY_CHECKS = 1;

-- ============================================================================
-- 完成！种子数据已全部填充
-- 统计: 24用户 | 63技能 | 20货架 | 16需求 | 14订单 | 24积分账户 | 8评价
--       8聊天会话 | 17消息 | 12通知 | 1违规 | 1申诉 | 5校园认证
--       8信任分记录 | 18签到 | 14时间格子 | 11统计日 | 15热度 | 5日志
--       + 补充: dbs_point_sign_in 表, hidden 字段, handle_result 字段, token_version 字段
-- ============================================================================
