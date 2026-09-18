-- ============================================================================
-- 搭把手 H2 测试迁移脚本 V1.18.0
-- 描述: 完整种子数据（H2 兼容版）
-- 说明: 跳过 schema 补充（已由 V1.14.0/V1.16.0/V1.17.0/V1.17.1 处理）
--       使用 DATEADD 替代 MySQL INTERVAL 语法
-- ============================================================================

-- 与同版本 MySQL 迁移保持一致，种子订单写入前先补齐核销和退款字段。
ALTER TABLE `dbs_order` ADD COLUMN `buyer_verify_code` VARCHAR(6) DEFAULT NULL COMMENT '开始核销码-买家持有';
ALTER TABLE `dbs_order` ADD COLUMN `seller_verify_code` VARCHAR(6) DEFAULT NULL COMMENT '开始核销码-卖家持有';
ALTER TABLE `dbs_order` ADD COLUMN `buyer_confirm_code` VARCHAR(6) DEFAULT NULL COMMENT '完成确认码-买家持有';
ALTER TABLE `dbs_order` ADD COLUMN `seller_confirm_code` VARCHAR(6) DEFAULT NULL COMMENT '完成确认码-卖家持有';
ALTER TABLE `dbs_order` ADD COLUMN `buyer_verified` TINYINT DEFAULT 0 COMMENT '买家是否已输入核销码';
ALTER TABLE `dbs_order` ADD COLUMN `seller_verified` TINYINT DEFAULT 0 COMMENT '卖家是否已输入核销码';
ALTER TABLE `dbs_order` ADD COLUMN `buyer_confirmed` TINYINT DEFAULT 0 COMMENT '买家是否已输入确认码';
ALTER TABLE `dbs_order` ADD COLUMN `seller_confirmed` TINYINT DEFAULT 0 COMMENT '卖家是否已输入确认码';
ALTER TABLE `dbs_order` ADD COLUMN `refund_requester` VARCHAR(10) DEFAULT NULL COMMENT '退款发起方: buyer/seller';
ALTER TABLE `dbs_order` ADD COLUMN `refund_agreed` TINYINT DEFAULT 0 COMMENT '退款是否已同意';

-- ==================== 1. 清理所有种子数据 ====================

DELETE FROM `sys_log`;
DELETE FROM `stat_skill_heat`;
DELETE FROM `stat_daily_summary`;
DELETE FROM `dbs_time_slot`;
DELETE FROM `dbs_point_sign_in`;
DELETE FROM `dbs_user_trust_score_log`;
DELETE FROM `dbs_user_campus_auth`;
DELETE FROM `dbs_notification`;
DELETE FROM `credit_appeal`;
DELETE FROM `credit_violation`;
DELETE FROM `dbs_chat_message`;
DELETE FROM `dbs_chat_session`;
DELETE FROM `dbs_review`;
DELETE FROM `dbs_guarantee_pool`;
DELETE FROM `dbs_point_freeze`;
DELETE FROM `dbs_point_transaction`;
DELETE FROM `dbs_point_account`;
DELETE FROM `dbs_order`;
DELETE FROM `dbs_demand`;
DELETE FROM `dbs_skill_shelf`;
DELETE FROM `dbs_user_skill`;
DELETE FROM `sys_role_permission`;
DELETE FROM `sys_permission`;
DELETE FROM `sys_user_role`;
DELETE FROM `sys_role`;
DELETE FROM `sys_config`;
DELETE FROM `dbs_user`;

-- ==================== 2. 用户数据 (24人) ====================

INSERT INTO `dbs_user` (`id`, `username`, `nickname`, `avatar`, `phone`, `password_hash`, `point_balance`, `trust_score`, `campus`, `building`, `bio`, `status`, `longitude`, `latitude`) VALUES
(1, 'admin', '管理员', NULL, '13800000000', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', 99999, 5.0, NULL, NULL, '系统管理员', 1, NULL, NULL),
(2, 'zhangsan', '张三', 'https://api.dicebear.com/7.x/avataaars/svg?seed=zhangsan', '13800000001', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', 200, 4.5, '主校区', '1号宿舍楼', '计算机学院大四', 1, 116.397428, 39.90923),
(3, 'lisi', '李四', 'https://api.dicebear.com/7.x/avataaars/svg?seed=lisi', '13800000002', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', 500, 4.8, '东校区', '2号宿舍楼', '设计学院大三', 1, 116.40528, 39.90421),
(4, 'wangwu', '王五', 'https://api.dicebear.com/7.x/avataaars/svg?seed=wangwu', '13800000003', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', 300, 4.2, '西校区', '3号宿舍楼', '体育学院', 1, 116.38915, 39.91234),
(5, 'chenqi', '陈七', 'https://api.dicebear.com/7.x/avataaars/svg?seed=chenqi', '13800000004', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', 350, 4.8, '主校区', '4号宿舍楼', '计算机学院大三', 1, 116.397428, 39.90923),
(6, 'zhaoba', '赵八', 'https://api.dicebear.com/7.x/avataaars/svg?seed=zhaoba', '13800000005', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', 180, 3.5, '东校区', '5号宿舍楼', '艺术学院', 1, 116.40528, 39.90421),
(7, 'sunjiu', '孙九', 'https://api.dicebear.com/7.x/avataaars/svg?seed=sunjiu', '13800000006', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', 420, 4.2, '主校区', '6号宿舍楼', '数学系学霸', 1, 116.39812, 39.91015),
(8, 'zhouyi', '周十', 'https://api.dicebear.com/7.x/avataaars/svg?seed=zhouyi', '13800000007', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', 90, 2.8, '西校区', '1号宿舍楼', '大一新生', 1, 116.38915, 39.91234),
(9, 'wuer', '吴二', 'https://api.dicebear.com/7.x/avataaars/svg?seed=wuer', '13800000008', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', 600, 4.9, '主校区', '7号宿舍楼', '研究生', 1, 116.39688, 39.90856),
(10, 'zhengsan', '郑三', 'https://api.dicebear.com/7.x/avataaars/svg?seed=zhengsan', '13800000009', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', 250, 3.8, '东校区', '2号宿舍楼', '音乐学院', 1, 116.40455, 39.90378),
(11, 'huangsi', '黄四', 'https://api.dicebear.com/7.x/avataaars/svg?seed=huangsi', '13800000010', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', 150, 3.2, '主校区', '8号宿舍楼', '体育生', 1, 116.39777, 39.90989),
(12, 'linwu', '林五', 'https://api.dicebear.com/7.x/avataaars/svg?seed=linwu', '13800000011', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', 380, 4.5, '西校区', '2号宿舍楼', '英语专业', 1, 116.38892, 39.91198),
(13, 'hexiao', '何小', 'https://api.dicebear.com/7.x/avataaars/svg?seed=hexiao', '13800000012', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', 200, 3.9, '主校区', '9号宿舍楼', '设计学院', 1, 116.39801, 39.90765),
(14, 'luda', '卢大', 'https://api.dicebear.com/7.x/avataaars/svg?seed=luda', '13800000013', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', 500, 4.7, '东校区', '3号宿舍楼', '全栈工程师', 1, 116.40388, 39.90456),
(15, 'xietian', '谢天', 'https://api.dicebear.com/7.x/avataaars/svg?seed=xietian', '13800000014', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', 120, 3.0, '主校区', '10号宿舍楼', '化学系', 1, 116.39712, 39.91078),
(16, 'caoxi', '曹曦', 'https://api.dicebear.com/7.x/avataaars/svg?seed=caoxi', '13800000015', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', 280, 4.1, '西校区', '3号宿舍楼', '法学院', 1, 116.38978, 39.91145),
(17, 'gaoyun', '高云', 'https://api.dicebear.com/7.x/avataaars/svg?seed=gaoyun', '13800000016', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', 350, 4.4, '主校区', '11号宿舍楼', '物理系', 1, 116.39845, 39.90812),
(18, 'tangmin', '唐敏', 'https://api.dicebear.com/7.x/avataaars/svg?seed=tangmin', '13800000017', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', 400, 4.6, '东校区', '4号宿舍楼', '医学院', 1, 116.40278, 39.90512),
(19, 'hanmei', '韩梅', 'https://api.dicebear.com/7.x/avataaars/svg?seed=hanmei', '13800000018', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', 180, 3.6, '主校区', '12号宿舍楼', '新闻学院', 1, 116.39655, 39.91134),
(20, 'fengyi', '冯一', 'https://api.dicebear.com/7.x/avataaars/svg?seed=fengyi', '13800000019', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', 550, 4.8, '西校区', '4号宿舍楼', '经济学院', 1, 116.39012, 39.91089),
(21, 'dengchao', '邓超', 'https://api.dicebear.com/7.x/avataaars/svg?seed=dengchao', '13800000020', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', 300, 4.0, '主校区', '13号宿舍楼', '机械学院', 1, 116.39788, 39.90945),
(22, 'pengpeng', '彭鹏', 'https://api.dicebear.com/7.x/avataaars/svg?seed=pengpeng', '13800000021', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', 100, 2.5, '东校区', '5号宿舍楼', '历史系', 1, 116.40412, 39.90345),
(23, 'xiaohua', '小花', 'https://api.dicebear.com/7.x/avataaars/svg?seed=xiaohua', '13800000022', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', 450, 4.3, '主校区', '14号宿舍楼', '外国语学院', 1, 116.39698, 39.91023),
(24, 'dawei', '大卫', 'https://api.dicebear.com/7.x/avataaars/svg?seed=dawei', '13800000023', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', 320, 4.1, '西校区', '5号宿舍楼', '建筑学院', 1, 116.38945, 39.91178);

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

INSERT IGNORE INTO `sys_config` (`config_key`, `config_value`, `config_name`, `config_type`, `description`) VALUES
('site.name',                   '搭把手',  '站点名称',              'business', '前台展示的平台名称'),
('site.notice',                 '欢迎使用搭把手技能互助平台！', '站点公告', 'business', '首页公告'),
('order.auto_cancel_minutes',   '30',      '订单自动取消分钟数',    'business', '待支付订单自动取消时间'),
('order.confirm_timeout_hours', '72',      '确认超时时间',          'business', '服务核销后买家确认超时时间'),
('point.register_bonus',        '100',     '注册奖励积分',          'business', '新用户注册赠送积分'),
('point.sign_in_reward',        '5',       '签到奖励积分',          'business', '每日签到奖励'),
('point.review_bonus',          '10',      '评价奖励积分',          'business', '完成评价奖励积分'),
('credit.violation_penalty',    '0.5',     '违规默认扣分',          'business', '管理员处理违规时的默认扣分'),
('trust.newcomer_max',          '2.9',     '新人最高分',            'business', '新人等级上限'),
('trust.reliable_max',          '3.9',     '靠谱最高分',            'business', '靠谱等级上限');

-- ==================== 5. 用户技能 ====================

INSERT INTO `dbs_user_skill` (`user_id`, `skill_tag_id`, `proficiency`, `description`) VALUES
(2, 16, 4, 'Python全栈开发'), (2, 17, 3, 'Java Spring Boot'), (2, 18, 3, 'Vue3+TypeScript'), (2, 19, 3, '微信小程序'),
(3, 11, 4, '海报设计'), (3, 12, 3, 'Logo/VI设计'), (3, 13, 3, 'Figma UI设计'), (3, 14, 4, 'PPT美化'),
(4, 26, 4, '篮球校队后卫'), (4, 27, 3, '健身教练证'),
(5, 16, 4, 'Python竞赛金牌'), (5, 17, 3, 'Java项目经验'), (5, 19, 3, '微信小程序'),
(6, 11, 3, '海报设计'), (6, 15, 4, '专业摄影师'), (6, 13, 3, 'Figma'),
(7, 1, 4, '高数答疑'), (7, 3, 3, '考研数学145'), (7, 4, 2, '论文排版'),
(8, 26, 1, '想学篮球'), (8, 31, 1, '想学吉他'),
(9, 16, 4, '深度学习博士'), (9, 20, 3, '服务器部署'), (9, 17, 3, '分布式系统'),
(10, 31, 4, '吉他十级'), (10, 32, 3, '声乐辅导'),
(11, 26, 4, '校队后卫'), (11, 27, 3, '健身教练证'),
(12, 2, 4, '专八优秀'), (12, 4, 3, 'SCI论文润色'),
(13, 13, 4, 'UI设计'), (13, 14, 3, 'PPT模板设计'),
(14, 16, 3, 'Python后端'), (14, 17, 4, 'Java架构师'), (14, 18, 4, 'React/Vue精通'),
(15, 1, 2, '基础化学辅导'),
(16, 4, 3, '论文写作指导'), (16, 2, 2, '英语六级580'),
(17, 1, 3, '大学物理辅导'), (17, 5, 2, 'Python数据分析'),
(18, 1, 3, '生物化学辅导'), (18, 27, 2, '运动康复知识'),
(19, 11, 2, '新闻海报设计'), (19, 14, 3, 'PPT演讲培训'),
(20, 1, 3, '高数/线代/概率论'), (20, 4, 3, '学术论文指导'),
(21, 6, 3, '电脑硬件维修'), (21, 19, 2, '嵌入式开发'),
(22, 4, 2, '文科论文写作'),
(23, 2, 3, '日语N1'), (23, 31, 2, '声乐基础'),
(24, 11, 3, '建筑效果图设计'), (24, 15, 4, '手绘/板绘');

-- ==================== 6. 技能货架 ====================

INSERT INTO `dbs_skill_shelf` (`id`, `user_id`, `skill_tag_id`, `title`, `description`, `point_price`, `duration_minutes`, `location_type`, `status`) VALUES
(1,  2,  16, 'Python编程辅导',   '从零基础到项目实战',         150, 60,  1, 1),
(2,  2,  18, 'Vue3前端开发辅导',  'Vue3+TypeScript全栈教学',    180, 90,  1, 1),
(3,  3,  11, '海报/宣传图设计',   '社团招新/活动宣传海报设计',  200, 120, 3, 1),
(4,  3,  14, 'PPT美化/答辩PPT',   '毕业答辩PPT设计美化',       120, 60,  1, 1),
(5,  4,  26, '篮球技术教学',      '投篮/运球/过人技巧',        100, 60,  2, 1),
(6,  5,  16, 'Python算法辅导',    'ACM银牌选手教你刷题',       200, 90,  1, 1),
(7,  5,  19, '微信小程序开发',    '从零开始教你做小程序',       180, 60,  1, 1),
(8,  6,  15, '专业摄影修图',      '人像/风景/产品摄影后期',     250, 120, 3, 1),
(9,  7,  1,  '高数期末冲刺',      '针对期末考试重点讲解',       150, 90,  1, 1),
(10, 7,  3,  '考研数学辅导',      '数一/数二/数三全面辅导',     300, 120, 1, 1),
(11, 9,  16, '深度学习入门',      'PyTorch实战',                350, 120, 1, 1),
(12, 10, 31, '吉他零基础教学',    '从和弦开始，一个月能弹唱',   120, 60,  2, 1),
(13, 11, 26, '篮球技术提升',      '投篮/运篮/过人技巧',        100, 60,  2, 1),
(14, 12, 2,  '英语口语陪练',      '全英文对话，纠正发音',       130, 60,  1, 1),
(15, 13, 13, 'APP界面设计',       '从原型到高保真',             280, 120, 1, 1),
(16, 14, 17, 'Java项目实战',      '带你从零搭建Spring Boot',    250, 120, 1, 1),
(17, 14, 18, 'React前端开发',     'Hooks+TypeScript实战',       220, 90,  1, 1),
(18, 17, 1,  '大学物理答疑',      '力学/电磁学/光学',          130, 60,  3, 1),
(19, 20, 1,  '概率论与数理统计',  '期末重点梳理',              160, 90,  1, 1),
(20, 24, 15, '手绘插画教学',      '素描/水彩/板绘',            200, 90,  2, 1);

-- ==================== 7. 需求 ====================

INSERT INTO `dbs_demand` (`id`, `user_id`, `skill_tag_id`, `title`, `description`, `point_reward`, `deadline`, `location_type`, `campus`, `building`, `status`, `demand_type`) VALUES
(1,  8,  16, 'Python课设求助',     '大一Python课设不会写',        200, DATEADD('DAY', 18, CURRENT_TIMESTAMP), 1, '西校区', '1号宿舍楼',  1, 1),
(2,  8,  31, '想学吉他弹唱',       '零基础想学吉他',              150, DATEADD('DAY', 30, CURRENT_TIMESTAMP), 2, '西校区', '1号宿舍楼',  1, 1),
(3,  6,  17, 'Java Web大作业',     '需要一个完整的Web项目',       300, DATEADD('DAY', 13, CURRENT_TIMESTAMP), 1, '东校区', '5号宿舍楼',  1, 1),
(4,  11, 11, '社团招新海报设计',   '武术社需要一张中国风海报',    120, DATEADD('DAY', 6, CURRENT_TIMESTAMP),  1, '主校区', '8号宿舍楼',  1, 1),
(5,  15, 6,  '电脑开不了机',       '笔记本突然黑屏',              80,  DATEADD('DAY', 1, CURRENT_TIMESTAMP),  2, '主校区', '10号宿舍楼', 1, 1),
(6,  19, 16, '爬虫脚本编写',       '需要爬取公开数据',            180, DATEADD('DAY', 10, CURRENT_TIMESTAMP), 1, '主校区', '12号宿舍楼', 1, 1),
(7,  22, 14, '毕业论文PPT',        '下周答辩需要精美PPT',         150, DATEADD('DAY', 3, CURRENT_TIMESTAMP),  1, '东校区', '5号宿舍楼',  1, 1),
(8,  18, 26, '篮球入门教学',       '完全不会打篮球',              100, DATEADD('DAY', 18, CURRENT_TIMESTAMP), 2, '东校区', '4号宿舍楼',  1, 1),
(9,  21, 13, '产品原型设计',       '创业项目需要UI原型',          250, DATEADD('DAY', 16, CURRENT_TIMESTAMP), 1, '主校区', '13号宿舍楼', 1, 1),
(10, 7,  2,  '英语六级辅导',       '12月考六级',                  200, DATEADD('DAY', 150, CURRENT_TIMESTAMP), 1, '主校区', '6号宿舍楼',  1, 1),
(11, 12, 18, 'Vue3项目重构',       '老项目升级到Vue3',            300, DATEADD('DAY', 23, CURRENT_TIMESTAMP), 1, '西校区', '2号宿舍楼',  1, 1),
(12, 23, 15, '产品宣传图设计',     '需要一套产品宣传图',          350, DATEADD('DAY', 13, CURRENT_TIMESTAMP), 1, '主校区', '14号宿舍楼', 1, 1),
(13, 2,  2,  '英语口语陪练',       '想提高日常英语口语',          100, DATEADD('DAY', 20, CURRENT_TIMESTAMP), 1, '主校区', '1号宿舍楼',  1, 2),
(14, 3,  16, '数据分析脚本',       '需要Python数据分析脚本',      200, DATEADD('DAY', 7, CURRENT_TIMESTAMP),  1, '东校区', '2号宿舍楼',  1, 1),
(15, 4,  11, '球队队服设计',       '篮球队需要队服设计方案',      180, DATEADD('DAY', 14, CURRENT_TIMESTAMP), 1, '西校区', '3号宿舍楼',  1, 1),
(16, 9,  4,  '论文润色',           'SCI论文英语润色',             250, DATEADD('DAY', 10, CURRENT_TIMESTAMP), 1, '主校区', '7号宿舍楼',  1, 1);

-- ==================== 8. 订单 ====================

INSERT INTO `dbs_order` (`id`, `order_no`, `buyer_id`, `seller_id`, `demand_id`, `skill_shelf_id`, `skill_tag_id`, `title`, `point_amount`, `status`,
  `buyer_verify_code`, `seller_verify_code`, `buyer_confirm_code`, `seller_confirm_code`,
  `buyer_verified`, `seller_verified`, `buyer_confirmed`, `seller_confirmed`,
  `refund_requester`, `refund_agreed`, `remark`, `create_time`, `update_time`) VALUES
(1,  'DBS20260625001', 3,  2,  NULL, 1,  16, 'Python编程辅导',   150, 5, 'A1B2C3', 'D4E5F6', 'G7H8I9', 'J0K1L2', 1, 1, 1, 1, NULL, 0, '老师讲得很好',     DATEADD('DAY', -7, CURRENT_TIMESTAMP), DATEADD('DAY', -6, CURRENT_TIMESTAMP)),
(2,  'DBS20260626001', 4,  3,  NULL, 3,  11, '海报设计',         180, 5, 'M3N4O5', 'P6Q7R8', 'S9T0U1', 'V2W3X4', 1, 1, 1, 1, NULL, 0, '设计效果超预期',   DATEADD('DAY', -6, CURRENT_TIMESTAMP), DATEADD('DAY', -5, CURRENT_TIMESTAMP)),
(3,  'DBS20260627001', 2,  4,  NULL, 5,  26, '篮球陪练',         120, 5, 'Y5Z6A7', 'B8C9D0', 'E1F2G3', 'H4I5J6', 1, 1, 1, 1, NULL, 0, '技术很好',         DATEADD('DAY', -5, CURRENT_TIMESTAMP), DATEADD('DAY', -4, CURRENT_TIMESTAMP)),
(4,  'DBS20260628001', 8,  7,  1,   NULL, 1,  '高数期末冲刺',     150, 5, 'K7L8M9', 'N0O1P2', 'Q3R4S5', 'T6U7V8', 1, 1, 1, 1, NULL, 0, '讲得很清楚',       DATEADD('DAY', -4, CURRENT_TIMESTAMP), DATEADD('DAY', -3, CURRENT_TIMESTAMP)),
(5,  'DBS20260628002', 6,  5,  NULL, 1,  16, 'Python算法辅导',   200, 5, 'W9X0Y1', 'Z2A3B4', 'C5D6E7', 'F8G9H0', 1, 1, 1, 1, NULL, 0, '学到很多',         DATEADD('DAY', -4, CURRENT_TIMESTAMP), DATEADD('DAY', -3, CURRENT_TIMESTAMP)),
(6,  'DBS20260629001', 8,  10, NULL, 12, 31, '吉他零基础教学',   120, 3, 'I1J2K3', 'L4M5N6', 'O7P8Q9', 'R0S1T2', 1, 1, 0, 0, NULL, 0, NULL,              DATEADD('DAY', -3, CURRENT_TIMESTAMP), DATEADD('DAY', -2, CURRENT_TIMESTAMP)),
(7,  'DBS20260629002', 15, 12, NULL, 14, 2,  '英语口语陪练',     130, 3, 'U3V4W5', 'X6Y7Z8', 'A9B0C1', 'D2E3F4', 1, 1, 0, 0, NULL, 0, NULL,              DATEADD('DAY', -3, CURRENT_TIMESTAMP), DATEADD('DAY', -2, CURRENT_TIMESTAMP)),
(8,  'DBS20260630001', 22, 14, NULL, 16, 17, 'Java项目实战',     250, 3, 'G5H6I7', 'J8K9L0', 'M1N2O3', 'P4Q5R6', 1, 1, 0, 0, NULL, 0, NULL,              DATEADD('DAY', -2, CURRENT_TIMESTAMP), DATEADD('DAY', -1, CURRENT_TIMESTAMP)),
(9,  'DBS20260630002', 11, 5,  NULL, 7,  19, '微信小程序开发',   180, 3, 'S7T8U9', 'V0W1X2', 'Y3Z4A5', 'B6C7D8', 1, 1, 1, 0, NULL, 0, '已学完',           DATEADD('DAY', -4, CURRENT_TIMESTAMP), DATEADD('DAY', -1, CURRENT_TIMESTAMP)),
(10, 'DBS20260630003', 19, 13, NULL, 15, 13, 'APP界面设计',      280, 3, 'E9F0G1', 'H2I3J4', 'K5L6M7', 'N8O9P0', 1, 1, 0, 1, NULL, 0, '设计完成',         DATEADD('DAY', -3, CURRENT_TIMESTAMP), DATEADD('DAY', -1, CURRENT_TIMESTAMP)),
(11, 'DBS20260630004', 7,  9,  NULL, 11, 16, '深度学习入门',     350, 1, 'Q1R2S3', 'T4U5V6', NULL, NULL, 0, 0, 0, 0, NULL, 0, NULL,              DATEADD('DAY', -2, CURRENT_TIMESTAMP), DATEADD('DAY', -2, CURRENT_TIMESTAMP)),
(12, 'DBS20260630005', 18, 11, NULL, 13, 26, '篮球技术提升',     100, 1, 'W7X8Y9', 'Z0A1B2', NULL, NULL, 0, 0, 0, 0, NULL, 0, NULL,              DATEADD('DAY', -2, CURRENT_TIMESTAMP), DATEADD('DAY', -2, CURRENT_TIMESTAMP)),
(13, 'DBS20260630006', 21, 17, NULL, 9,  1,  '大学物理答疑',     130, 1, 'C3D4E5', 'F6G7H8', NULL, NULL, 1, 0, 0, 0, NULL, 0, NULL,              DATEADD('DAY', -1, CURRENT_TIMESTAMP), DATEADD('DAY', -1, CURRENT_TIMESTAMP)),
(14, 'DBS20260625002', 2,  3,  NULL, 4,  14, 'PPT美化',          100, 0, 'I9J0K1', 'L2M3N4', NULL, NULL, 0, 0, 0, 0, NULL, 0, '临时有事取消',     DATEADD('DAY', -7, CURRENT_TIMESTAMP), DATEADD('DAY', -7, CURRENT_TIMESTAMP)),
(15, 'DBS20260629003', 4,  2,  NULL, 2,  18, '前端页面开发',     200, 7, 'O5P6Q7', 'R8S9T0', 'U1V2W3', 'X4Y5Z6', 1, 1, 1, 1, NULL, 0, '效果不满意',       DATEADD('DAY', -3, CURRENT_TIMESTAMP), DATEADD('DAY', -2, CURRENT_TIMESTAMP)),
(16, 'DBS20260630007', 23, 14, NULL, 17, 18, 'React前端开发',    220, 3, 'A7B8C9', 'D0E1F2', 'G3H4I5', 'J6K7L8', 1, 1, 0, 0, 'buyer', 0, NULL, DATEADD('DAY', -2, CURRENT_TIMESTAMP), DATEADD('DAY', -1, CURRENT_TIMESTAMP));

-- ==================== 9. 积分账户 ====================

INSERT INTO `dbs_point_account` (`user_id`, `available`, `frozen`, `total_earned`, `total_spent`) VALUES
(1,  99999, 0,    99999, 0), (2,  650, 200, 800, 350), (3,  480, 0, 630, 330),
(4,  180,   0,    300,   240), (5,  520, 350, 750, 200), (6,  130, 0, 280, 300),
(7,  300,   0,    450,   300), (8,  60,  120, 150, 210), (9,  950, 250, 1200, 0),
(10, 500,   0,    620,   240), (11, 100, 180, 200, 220), (12, 430, 130, 560, 0),
(13, 400,   280,  680,   0),   (14, 850, 250, 1100, 0),  (15, 50, 130, 130, 180),
(16, 200,   0,    350,   230), (17, 450, 0, 580, 260),   (18, 300, 0, 400, 200),
(19, 100,   280,  250,   330), (20, 500, 0, 700, 350),   (21, 380, 0, 480, 200),
(22, 50,    250,  150,   200), (23, 380, 220, 530, 300),  (24, 520, 200, 720, 0);

-- ==================== 10. 积分流水 ====================

INSERT INTO `dbs_point_transaction` (`user_id`, `order_id`, `type`, `amount`, `balance_after`, `description`, `create_time`) VALUES
(2,  NULL, 5, 100, 100, '新用户注册奖励', DATEADD('DAY', -12, CURRENT_TIMESTAMP)),
(3,  NULL, 5, 100, 100, '新用户注册奖励', DATEADD('DAY', -12, CURRENT_TIMESTAMP)),
(4,  NULL, 5, 100, 100, '新用户注册奖励', DATEADD('DAY', -12, CURRENT_TIMESTAMP)),
(5,  NULL, 5, 100, 100, '新用户注册奖励', DATEADD('DAY', -12, CURRENT_TIMESTAMP)),
(6,  NULL, 5, 100, 100, '新用户注册奖励', DATEADD('DAY', -11, CURRENT_TIMESTAMP)),
(7,  NULL, 5, 100, 100, '新用户注册奖励', DATEADD('DAY', -11, CURRENT_TIMESTAMP)),
(8,  NULL, 5, 100, 100, '新用户注册奖励', DATEADD('DAY', -11, CURRENT_TIMESTAMP)),
(9,  NULL, 5, 100, 100, '新用户注册奖励', DATEADD('DAY', -10, CURRENT_TIMESTAMP)),
(10, NULL, 5, 100, 100, '新用户注册奖励', DATEADD('DAY', -10, CURRENT_TIMESTAMP)),
(2,  1,  1, 150, 250,  '订单DBS20260625001收入', DATEADD('DAY', -7, CURRENT_TIMESTAMP)),
(3,  2,  1, 180, 280,  '订单DBS20260626001收入', DATEADD('DAY', -6, CURRENT_TIMESTAMP)),
(4,  3,  1, 120, 220,  '订单DBS20260627001收入', DATEADD('DAY', -5, CURRENT_TIMESTAMP)),
(7,  4,  1, 150, 350,  '订单DBS20260628001收入', DATEADD('DAY', -4, CURRENT_TIMESTAMP)),
(5,  5,  1, 200, 400,  '订单DBS20260628002收入', DATEADD('DAY', -4, CURRENT_TIMESTAMP)),
(3,  1,  2, 150, 130,  '订单DBS20260625001支出', DATEADD('DAY', -7, CURRENT_TIMESTAMP)),
(4,  2,  2, 180, 40,   '订单DBS20260626001支出', DATEADD('DAY', -6, CURRENT_TIMESTAMP)),
(2,  3,  2, 120, 130,  '订单DBS20260627001支出', DATEADD('DAY', -5, CURRENT_TIMESTAMP)),
(8,  6,  3, 120, 60,   '订单DBS20260629001冻结', DATEADD('DAY', -3, CURRENT_TIMESTAMP)),
(15, 7,  3, 130, 50,   '订单DBS20260629002冻结', DATEADD('DAY', -3, CURRENT_TIMESTAMP)),
(22, 8,  3, 250, 50,   '订单DBS20260630001冻结', DATEADD('DAY', -2, CURRENT_TIMESTAMP)),
(11, 9,  3, 180, 100,  '订单DBS20260630002冻结', DATEADD('DAY', -4, CURRENT_TIMESTAMP)),
(19, 10, 3, 280, 100,  '订单DBS20260630003冻结', DATEADD('DAY', -3, CURRENT_TIMESTAMP)),
(23, 16, 3, 220, 380,  '订单DBS20260630007冻结', DATEADD('DAY', -2, CURRENT_TIMESTAMP));

-- ==================== 11. 积分冻结记录 ====================

INSERT INTO `dbs_point_freeze` (`order_id`, `user_id`, `amount`, `status`, `freeze_time`, `release_time`) VALUES
(6,  8,  120, 1, DATEADD('DAY', -3, CURRENT_TIMESTAMP), NULL),
(7,  15, 130, 1, DATEADD('DAY', -3, CURRENT_TIMESTAMP), NULL),
(8,  22, 250, 1, DATEADD('DAY', -2, CURRENT_TIMESTAMP), NULL),
(9,  11, 180, 1, DATEADD('DAY', -4, CURRENT_TIMESTAMP), NULL),
(10, 19, 280, 1, DATEADD('DAY', -3, CURRENT_TIMESTAMP), NULL),
(16, 23, 220, 1, DATEADD('DAY', -2, CURRENT_TIMESTAMP), NULL);

-- ==================== 12. 担保池 ====================

INSERT INTO `dbs_guarantee_pool` (`order_id`, `amount`, `status`, `settle_time`) VALUES
(1, 150, 2, DATEADD('DAY', -7, CURRENT_TIMESTAMP)),
(2, 180, 2, DATEADD('DAY', -6, CURRENT_TIMESTAMP)),
(3, 120, 2, DATEADD('DAY', -5, CURRENT_TIMESTAMP)),
(6, 120, 1, NULL), (7, 130, 1, NULL), (8, 250, 1, NULL),
(9, 180, 1, NULL), (10, 280, 1, NULL), (16, 220, 1, NULL);

-- ==================== 13. 评价 ====================

INSERT INTO `dbs_review` (`order_id`, `reviewer_id`, `reviewee_id`, `rating`, `content`, `is_anonymous`, `create_time`) VALUES
(1, 3,  2,  5, '张三讲Python很清楚', 0, DATEADD('DAY', -7, CURRENT_TIMESTAMP)),
(1, 2,  3,  5, '李四学东西很快', 0, DATEADD('DAY', -7, CURRENT_TIMESTAMP)),
(2, 4,  3,  5, '海报设计太好了', 0, DATEADD('DAY', -6, CURRENT_TIMESTAMP)),
(2, 3,  4,  4, '需求沟通很顺畅', 0, DATEADD('DAY', -6, CURRENT_TIMESTAMP)),
(3, 2,  4,  4, '王五篮球技术好', 0, DATEADD('DAY', -5, CURRENT_TIMESTAMP)),
(3, 4,  2,  5, '张三进步很快', 0, DATEADD('DAY', -5, CURRENT_TIMESTAMP)),
(4, 8,  7,  5, '孙九讲高数太棒了', 0, DATEADD('DAY', -4, CURRENT_TIMESTAMP)),
(5, 6,  5,  5, '陈七Python算法含金量高', 0, DATEADD('DAY', -4, CURRENT_TIMESTAMP));

-- ==================== 14. 聊天会话与消息 ====================

INSERT INTO `dbs_chat_session` (`id`, `user1_id`, `user2_id`, `last_message`, `last_time`, `unread_count`, `create_time`, `update_time`) VALUES
(1, 2,  3,  '好的，明天下午2点开始', DATEADD('DAY', -7, CURRENT_TIMESTAMP), 0, DATEADD('DAY', -7, CURRENT_TIMESTAMP), DATEADD('DAY', -7, CURRENT_TIMESTAMP)),
(2, 2,  4,  '篮球约起来！', DATEADD('DAY', -5, CURRENT_TIMESTAMP), 1, DATEADD('DAY', -5, CURRENT_TIMESTAMP), DATEADD('DAY', -5, CURRENT_TIMESTAMP)),
(3, 5,  6,  'Python项目做完了吗？', DATEADD('DAY', -4, CURRENT_TIMESTAMP), 2, DATEADD('DAY', -4, CURRENT_TIMESTAMP), DATEADD('DAY', -4, CURRENT_TIMESTAMP)),
(4, 7,  8,  '高数还有不懂的可以问我', DATEADD('DAY', -4, CURRENT_TIMESTAMP), 0, DATEADD('DAY', -4, CURRENT_TIMESTAMP), DATEADD('DAY', -4, CURRENT_TIMESTAMP)),
(5, 10, 8,  '吉他调好弦了吗？', DATEADD('DAY', -3, CURRENT_TIMESTAMP), 1, DATEADD('DAY', -3, CURRENT_TIMESTAMP), DATEADD('DAY', -3, CURRENT_TIMESTAMP)),
(6, 12, 15, 'See you tomorrow!', DATEADD('DAY', -3, CURRENT_TIMESTAMP), 0, DATEADD('DAY', -3, CURRENT_TIMESTAMP), DATEADD('DAY', -3, CURRENT_TIMESTAMP)),
(7, 13, 19, '原型图发你看看', DATEADD('DAY', -2, CURRENT_TIMESTAMP), 1, DATEADD('DAY', -3, CURRENT_TIMESTAMP), DATEADD('DAY', -2, CURRENT_TIMESTAMP)),
(8, 14, 22, 'Spring Boot环境配好了吗？', DATEADD('DAY', -2, CURRENT_TIMESTAMP), 0, DATEADD('DAY', -2, CURRENT_TIMESTAMP), DATEADD('DAY', -2, CURRENT_TIMESTAMP));

INSERT INTO `dbs_chat_message` (`session_id`, `sender_id`, `content`, `msg_type`, `is_read`, `create_time`) VALUES
(1, 2, '你好，我看到你需要Python辅导？', 1, 1, DATEADD('DAY', -7, CURRENT_TIMESTAMP)),
(1, 3, '是的，我有个爬虫项目不会写', 1, 1, DATEADD('DAY', -7, CURRENT_TIMESTAMP)),
(1, 2, '没问题，明天下午有空吗？', 1, 1, DATEADD('DAY', -7, CURRENT_TIMESTAMP)),
(1, 3, '有空的，几点合适？', 1, 1, DATEADD('DAY', -7, CURRENT_TIMESTAMP)),
(1, 2, '好的，明天下午2点开始', 1, 1, DATEADD('DAY', -7, CURRENT_TIMESTAMP)),
(2, 4, '三哥，周末打篮球不？', 1, 1, DATEADD('DAY', -5, CURRENT_TIMESTAMP)),
(2, 2, '好啊，几点？', 1, 1, DATEADD('DAY', -5, CURRENT_TIMESTAMP)),
(2, 4, '下午3点，东校区篮球场', 1, 1, DATEADD('DAY', -5, CURRENT_TIMESTAMP)),
(2, 2, '篮球约起来！', 1, 0, DATEADD('DAY', -5, CURRENT_TIMESTAMP)),
(3, 5, '赵八，Python项目做完了吗？', 1, 1, DATEADD('DAY', -4, CURRENT_TIMESTAMP)),
(3, 6, '快了，还有个bug没修好', 1, 1, DATEADD('DAY', -4, CURRENT_TIMESTAMP)),
(3, 5, '需要帮忙吗？', 1, 1, DATEADD('DAY', -4, CURRENT_TIMESTAMP)),
(3, 6, '好的，晚上可以帮我看看吗？', 1, 0, DATEADD('DAY', -4, CURRENT_TIMESTAMP)),
(3, 5, 'Python项目做完了吗？', 1, 0, DATEADD('DAY', -4, CURRENT_TIMESTAMP)),
(5, 10, '周十，吉他调好弦了吗？', 1, 0, DATEADD('DAY', -3, CURRENT_TIMESTAMP)),
(5, 8,  '调好了，E弦好像有点松', 1, 1, DATEADD('DAY', -3, CURRENT_TIMESTAMP)),
(5, 10, '没关系，明天上课我帮你调', 1, 0, DATEADD('DAY', -3, CURRENT_TIMESTAMP));

-- ==================== 15. 通知 ====================

INSERT INTO `dbs_notification` (`user_id`, `type`, `title`, `content`, `related_type`, `related_id`, `is_read`, `create_time`) VALUES
(2,  'order',  '订单完成',     '订单DBS20260625001已完成', 'order',  1,  1, DATEADD('DAY', -7, CURRENT_TIMESTAMP)),
(3,  'order',  '订单完成',     '订单DBS20260626001已完成', 'order',  2,  1, DATEADD('DAY', -6, CURRENT_TIMESTAMP)),
(4,  'order',  '订单完成',     '订单DBS20260627001已完成', 'order',  3,  1, DATEADD('DAY', -5, CURRENT_TIMESTAMP)),
(8,  'order',  '新订单',       '新订单DBS20260629001待服务', 'order',  6,  0, DATEADD('DAY', -3, CURRENT_TIMESTAMP)),
(15, 'order',  '新订单',       '新订单DBS20260629002待服务', 'order',  7,  0, DATEADD('DAY', -3, CURRENT_TIMESTAMP)),
(2,  'credit', '收到评价',     '李四给您了5星好评', 'review', 1,  1, DATEADD('DAY', -7, CURRENT_TIMESTAMP)),
(3,  'credit', '收到评价',     '张三给您了5星好评', 'review', 2,  1, DATEADD('DAY', -7, CURRENT_TIMESTAMP)),
(7,  'point',  '积分到账',     '订单DBS20260628001收入150积分', NULL, NULL, 1, DATEADD('DAY', -4, CURRENT_TIMESTAMP)),
(5,  'point',  '积分到账',     '订单DBS20260628002收入200积分', NULL, NULL, 1, DATEADD('DAY', -4, CURRENT_TIMESTAMP)),
(8,  'system', '欢迎加入搭把手', '感谢注册，送您100积分！', NULL, NULL, 1, DATEADD('DAY', -12, CURRENT_TIMESTAMP)),
(22, 'order',  '订单争议',     '订单DBS20260629003存在争议', 'order', 15, 0, DATEADD('DAY', -2, CURRENT_TIMESTAMP)),
(11, 'order',  '待核销',       '订单DBS20260630004等待核销', 'order', 11, 0, DATEADD('DAY', -2, CURRENT_TIMESTAMP));

-- ==================== 16. 违规与申诉 ====================

INSERT INTO `credit_violation` (`user_id`, `order_id`, `type`, `description`, `penalty_score`, `reporter_id`, `status`, `create_time`) VALUES
(22, 15, 'other', '订单完成后恶意差评', 0.5, 2, 1, DATEADD('DAY', -2, CURRENT_TIMESTAMP));

INSERT INTO `credit_appeal` (`violation_id`, `appellant_id`, `reason`, `status`, `create_time`) VALUES
(1, 22, '我认为评价是客观的', 0, DATEADD('DAY', -2, CURRENT_TIMESTAMP));

-- ==================== 17. 校园认证 ====================

INSERT INTO `dbs_user_campus_auth` (`user_id`, `auth_type`, `student_no`, `real_name`, `campus`, `college`, `status`, `reviewer_id`, `review_time`, `create_time`) VALUES
(2,  'student', '2022001001', '张三丰', '主校区', '计算机科学与技术学院', 1, 1, DATEADD('DAY', -10, CURRENT_TIMESTAMP), DATEADD('DAY', -12, CURRENT_TIMESTAMP)),
(3,  'student', '2022002002', '李思思', '东校区', '艺术设计学院',         1, 1, DATEADD('DAY', -9, CURRENT_TIMESTAMP),  DATEADD('DAY', -11, CURRENT_TIMESTAMP)),
(5,  'student', '2023001005', '陈七七', '主校区', '计算机科学与技术学院', 1, 1, DATEADD('DAY', -8, CURRENT_TIMESTAMP),  DATEADD('DAY', -10, CURRENT_TIMESTAMP)),
(12, 'student', '2021003012', '林五五', '西校区', '外国语学院',           0, NULL, NULL, DATEADD('DAY', -2, CURRENT_TIMESTAMP)),
(20, 'student', '2022004020', '冯一一', '西校区', '经济管理学院',         1, 1, DATEADD('DAY', -5, CURRENT_TIMESTAMP),  DATEADD('DAY', -7, CURRENT_TIMESTAMP));

-- ==================== 18. 信任分变动记录 ====================

INSERT INTO `dbs_user_trust_score_log` (`user_id`, `order_id`, `type`, `score_change`, `score_before`, `score_after`, `reason`, `create_time`) VALUES
(2,  1,  'order_complete',   0.3, 4.2, 4.5, '订单DBS20260625001完成', DATEADD('DAY', -7, CURRENT_TIMESTAMP)),
(3,  2,  'order_complete',   0.3, 4.5, 4.8, '订单DBS20260626001完成', DATEADD('DAY', -6, CURRENT_TIMESTAMP)),
(4,  3,  'order_complete',   0.2, 4.0, 4.2, '订单DBS20260627001完成', DATEADD('DAY', -5, CURRENT_TIMESTAMP)),
(7,  4,  'order_complete',   0.2, 4.0, 4.2, '订单DBS20260628001完成', DATEADD('DAY', -4, CURRENT_TIMESTAMP)),
(5,  5,  'order_complete',   0.3, 4.5, 4.8, '订单DBS20260628002完成', DATEADD('DAY', -4, CURRENT_TIMESTAMP)),
(22, NULL, 'violation',      -0.5, 3.0, 2.5, '违规：恶意差评', DATEADD('DAY', -2, CURRENT_TIMESTAMP)),
(2,  NULL, 'review_bonus',    0.1, 4.4, 4.5, '完成评价奖励', DATEADD('DAY', -7, CURRENT_TIMESTAMP)),
(3,  NULL, 'review_bonus',    0.1, 4.7, 4.8, '完成评价奖励', DATEADD('DAY', -6, CURRENT_TIMESTAMP));

-- ==================== 19. 签到记录 ====================

INSERT INTO `dbs_point_sign_in` (`user_id`, `sign_date`, `reward`, `consecutive_days`, `create_time`) VALUES
(2,  DATEADD('DAY', -6, CURRENT_DATE), 5, 1, DATEADD('DAY', -6, CURRENT_TIMESTAMP)),
(2,  DATEADD('DAY', -5, CURRENT_DATE), 5, 2, DATEADD('DAY', -5, CURRENT_TIMESTAMP)),
(2,  DATEADD('DAY', -4, CURRENT_DATE), 5, 3, DATEADD('DAY', -4, CURRENT_TIMESTAMP)),
(2,  DATEADD('DAY', -3, CURRENT_DATE), 5, 4, DATEADD('DAY', -3, CURRENT_TIMESTAMP)),
(2,  DATEADD('DAY', -2, CURRENT_DATE), 5, 5, DATEADD('DAY', -2, CURRENT_TIMESTAMP)),
(3,  DATEADD('DAY', -5, CURRENT_DATE), 5, 1, DATEADD('DAY', -5, CURRENT_TIMESTAMP)),
(3,  DATEADD('DAY', -4, CURRENT_DATE), 5, 2, DATEADD('DAY', -4, CURRENT_TIMESTAMP)),
(3,  DATEADD('DAY', -3, CURRENT_DATE), 5, 3, DATEADD('DAY', -3, CURRENT_TIMESTAMP)),
(5,  DATEADD('DAY', -4, CURRENT_DATE), 5, 1, DATEADD('DAY', -4, CURRENT_TIMESTAMP)),
(5,  DATEADD('DAY', -3, CURRENT_DATE), 5, 2, DATEADD('DAY', -3, CURRENT_TIMESTAMP)),
(5,  DATEADD('DAY', -2, CURRENT_DATE), 5, 3, DATEADD('DAY', -2, CURRENT_TIMESTAMP)),
(5,  DATEADD('DAY', -1, CURRENT_DATE), 5, 4, DATEADD('DAY', -1, CURRENT_TIMESTAMP)),
(7,  DATEADD('DAY', -3, CURRENT_DATE), 5, 1, DATEADD('DAY', -3, CURRENT_TIMESTAMP)),
(9,  DATEADD('DAY', -2, CURRENT_DATE), 5, 1, DATEADD('DAY', -2, CURRENT_TIMESTAMP)),
(9,  DATEADD('DAY', -1, CURRENT_DATE), 5, 2, DATEADD('DAY', -1, CURRENT_TIMESTAMP)),
(12, DATEADD('DAY', -1, CURRENT_DATE), 5, 1, DATEADD('DAY', -1, CURRENT_TIMESTAMP)),
(14, DATEADD('DAY', -1, CURRENT_DATE), 5, 1, DATEADD('DAY', -1, CURRENT_TIMESTAMP)),
(20, DATEADD('DAY', -1, CURRENT_DATE), 5, 1, DATEADD('DAY', -1, CURRENT_TIMESTAMP));

-- ==================== 20. 时间格子 ====================

INSERT INTO `dbs_time_slot` (`user_id`, `date`, `start_time`, `end_time`, `status`) VALUES
(2,  CURRENT_DATE, '09:00:00', '11:00:00', 1),
(2,  CURRENT_DATE, '14:00:00', '16:00:00', 1),
(2,  DATEADD('DAY', 1, CURRENT_DATE), '09:00:00', '12:00:00', 1),
(2,  DATEADD('DAY', 2, CURRENT_DATE), '14:00:00', '17:00:00', 2),
(3,  CURRENT_DATE, '10:00:00', '12:00:00', 1),
(3,  CURRENT_DATE, '15:00:00', '17:00:00', 1),
(3,  DATEADD('DAY', 1, CURRENT_DATE), '09:00:00', '11:00:00', 1),
(5,  CURRENT_DATE, '09:00:00', '12:00:00', 1),
(5,  DATEADD('DAY', 1, CURRENT_DATE), '14:00:00', '18:00:00', 1),
(10, CURRENT_DATE, '10:00:00', '12:00:00', 2),
(10, DATEADD('DAY', 1, CURRENT_DATE), '14:00:00', '16:00:00', 1),
(14, CURRENT_DATE, '09:00:00', '12:00:00', 1),
(14, CURRENT_DATE, '14:00:00', '18:00:00', 2),
(14, DATEADD('DAY', 1, CURRENT_DATE), '09:00:00', '18:00:00', 1);

-- ==================== 21. 每日统计 ====================

INSERT INTO `stat_daily_summary` (`stat_date`, `new_user_count`, `active_user_count`, `new_demand_count`, `new_order_count`, `completed_order_count`, `cancelled_order_count`, `point_inflow`, `point_outflow`) VALUES
(DATEADD('DAY', -10, CURRENT_DATE), 4,  4,  0, 0, 0, 0, 400, 0),
(DATEADD('DAY', -9, CURRENT_DATE),  2,  6,  0, 0, 0, 0, 200, 0),
(DATEADD('DAY', -8, CURRENT_DATE),  3,  8,  1, 0, 0, 0, 300, 0),
(DATEADD('DAY', -7, CURRENT_DATE),  2, 10,  1, 2, 1, 0, 350, 270),
(DATEADD('DAY', -6, CURRENT_DATE),  1, 12,  1, 1, 1, 0, 280, 180),
(DATEADD('DAY', -5, CURRENT_DATE),  2, 15,  2, 1, 1, 1, 220, 120),
(DATEADD('DAY', -4, CURRENT_DATE),  1, 18,  1, 2, 2, 0, 450, 270),
(DATEADD('DAY', -3, CURRENT_DATE),  2, 20,  2, 3, 0, 0, 150, 250),
(DATEADD('DAY', -2, CURRENT_DATE),  1, 22,  3, 4, 0, 0, 200, 350),
(DATEADD('DAY', -1, CURRENT_DATE),  0, 20,  1, 0, 0, 0, 50,  0),
(CURRENT_DATE,                      0, 18,  0, 0, 0, 0, 0,   0);

-- ==================== 22. 技能热度统计 ====================

INSERT INTO `stat_skill_heat` (`stat_date`, `skill_tag_id`, `category_id`, `shelf_count`, `demand_count`, `order_count`, `heat_score`) VALUES
(CURRENT_DATE, 16, 4, 3, 2, 3, 95.5),
(CURRENT_DATE, 17, 4, 2, 2, 2, 88.0),
(CURRENT_DATE, 11, 3, 2, 2, 2, 85.5),
(CURRENT_DATE, 26, 5, 2, 1, 2, 82.0),
(CURRENT_DATE, 2,  1, 1, 2, 1, 78.5),
(CURRENT_DATE, 18, 4, 2, 1, 1, 75.0),
(CURRENT_DATE, 31, 6, 1, 1, 1, 72.0),
(CURRENT_DATE, 13, 3, 1, 1, 1, 70.0),
(CURRENT_DATE, 1,  1, 2, 1, 1, 68.5),
(CURRENT_DATE, 15, 3, 1, 1, 0, 65.0),
(CURRENT_DATE, 14, 3, 1, 1, 1, 62.0),
(CURRENT_DATE, 27, 5, 1, 1, 0, 58.0),
(CURRENT_DATE, 6,  2, 1, 1, 0, 55.0),
(CURRENT_DATE, 19, 4, 1, 0, 1, 52.0),
(CURRENT_DATE, 4,  1, 1, 1, 0, 50.0);

-- ==================== 23. 操作日志 ====================

INSERT INTO `sys_log` (`operator_id`, `operation_type`, `operation_content`, `method`, `request_url`, `ip`, `status`, `cost_time_ms`, `create_time`) VALUES
(1, 'LOGIN',     '管理员登录',                'POST', '/api/v1/auth/login',     '127.0.0.1', 1, 45, DATEADD('DAY', -2, CURRENT_TIMESTAMP)),
(1, 'USER_EDIT', '修改用户状态: user_id=22',  'PUT',  '/api/admin/v1/users/22/status', '127.0.0.1', 1, 32, DATEADD('DAY', -2, CURRENT_TIMESTAMP)),
(1, 'VIOLATION', '处理违规: violation_id=1',  'POST', '/api/admin/v1/violations/1', '127.0.0.1', 1, 28, DATEADD('DAY', -2, CURRENT_TIMESTAMP)),
(1, 'LOGIN',     '管理员登录',                'POST', '/api/v1/auth/login',     '127.0.0.1', 1, 38, DATEADD('DAY', -1, CURRENT_TIMESTAMP)),
(1, 'CONFIG',    '更新系统配置',              'PUT',  '/api/admin/v1/config',   '127.0.0.1', 1, 22, DATEADD('DAY', -1, CURRENT_TIMESTAMP));
