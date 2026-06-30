-- ============================================================================
-- 搭把手数据库迁移脚本 V1.7.0
-- 描述: 种子数据 — 管理员、技能分类、技能标签、测试用户
-- 作者: dabashou-dev
-- 日期: 2026-06-29
-- 依赖: V1.0.0 (dbs_user, dbs_skill_category, dbs_skill_tag, dbs_demand)
-- 变更记录:
--   V1.7.0 (2026-06-29) - 初始种子数据
-- ============================================================================

-- ==================== 1. 管理员 ====================
-- 密码: admin123
INSERT INTO `dbs_user` (`username`, `nickname`, `avatar`, `phone`, `password_hash`, `point_balance`, `trust_score`, `campus`, `building`, `bio`, `status`)
VALUES ('admin', '系统管理员', 'https://picsum.photos/seed/admin/200/200', '13800000000',
        '$2b$12$CK5TP/w35zJsWr2iNUTv0.gXhMp10hDNVPf9L3wSk7he.tOQJycIO', 99999, 5.0, '主校区', '行政楼', '搭把手平台管理员', 1);

-- ==================== 2. 测试用户 ====================
-- 密码: 123456
INSERT INTO `dbs_user` (`username`, `nickname`, `avatar`, `phone`, `password_hash`, `point_balance`, `trust_score`, `campus`, `building`, `bio`, `status`)
VALUES
('zhangsan', '张三', 'https://picsum.photos/seed/zhangsan/200/200', '13800000001',
 '$2b$12$VipmeVvp066DBkrRvDNIgerpnmKqvo.lLzB84IwiIBlEahgG/0sjW', 500, 5.0, '主校区', '1号宿舍楼', '热爱编程，擅长Java和Python', 1),
('lisi', '李四', 'https://picsum.photos/seed/lisi/200/200', '13800000002',
 '$2b$12$QVF7Ab0Isg0OsT1HNbCEOOiaNiC7q50MLXOwNXNgG3Ojs3JNDKd6G', 300, 4.5, '主校区', '2号宿舍楼', '平面设计师，熟练PS和AI', 1),
('wangwu', '王五', 'https://picsum.photos/seed/wangwu/200/200', '13800000003',
 '$2b$12$BrHm3.KTC8iyQ9PFmz6WOubq8QT47ofPcCIsFP6Dc7q1TyFAZqARO', 200, 4.0, '东校区', '3号宿舍楼', '运动达人，篮球和健身', 1);

-- ==================== 3. 技能分类 ====================
INSERT INTO `dbs_skill_category` (`name`, `icon`, `sort_order`) VALUES
('学业辅导', 'icon-study', 1),
('维修帮忙', 'icon-repair', 2),
('设计美工', 'icon-design', 3),
('技术支持', 'icon-coding', 4),
('运动陪练', 'icon-sports', 5),
('音乐艺术', 'icon-music', 6),
('生活服务', 'icon-service', 7);

-- ==================== 4. 技能标签 ====================
-- category_id 对应分类: 1=学业辅导, 2=维修帮忙, 3=设计美工, 4=技术支持, 5=运动陪练, 6=音乐艺术, 7=生活服务
INSERT INTO `dbs_skill_tag` (`category_id`, `name`) VALUES
-- 学业辅导
(1, '高等数学辅导'), (1, '英语四六级辅导'), (1, '考研辅导'), (1, '论文写作指导'), (1, '编程入门教学'),
-- 维修帮忙
(2, '电脑维修'), (2, '家具修理'), (2, '家电维修'), (2, '自行车修理'), (2, '电器检修'),
-- 设计美工
(3, '海报设计'), (3, 'Logo设计'), (3, 'UI界面设计'), (3, 'PPT美化'), (3, '摄影修图'),
-- 技术支持
(4, 'Python开发'), (4, 'Java开发'), (4, '前端开发'), (4, '小程序开发'), (4, '服务器运维'),
-- 运动陪练
(5, '篮球陪练'), (5, '健身指导'), (5, '游泳教学'), (5, '跑步陪练'), (5, '瑜伽教学'),
-- 音乐艺术
(6, '吉他教学'), (6, '声乐辅导'), (6, '钢琴陪练'), (6, '书法教学'), (6, '素描教学'),
-- 生活服务
(7, '取快递'), (7, '代买物资'), (7, '打扫卫生'), (7, '搬宿舍'), (7, '照顾宠物');

-- ==================== 5. 测试数据：用户技能 ====================
-- 张三(id=2) 的编程技能
INSERT INTO `dbs_user_skill` (`user_id`, `skill_tag_id`, `proficiency`, `description`) VALUES
(2, 16, 4, '精通Python，可接爬虫/数据分析项目'),  -- Python开发, 专家
(2, 17, 3, '熟悉Java Spring Boot，可帮debug'),  -- Java开发, 精通
(2, 18, 3, '熟练Vue.js，可帮忙写前端页面');       -- 前端开发, 精通

-- 李四(id=3) 的设计技能
INSERT INTO `dbs_user_skill` (`user_id`, `skill_tag_id`, `proficiency`, `description`) VALUES
(3, 11, 4, '专业海报设计，有获奖作品'),          -- 海报设计, 专家
(3, 12, 4, '可设计品牌Logo和VI'),               -- Logo设计, 专家
(3, 14, 3, 'PPT做得超好看');                     -- PPT美化, 精通

-- 王五(id=4) 的运动技能
INSERT INTO `dbs_user_skill` (`user_id`, `skill_tag_id`, `proficiency`, `description`) VALUES
(4, 26, 4, '校篮球队主力，可系统教学'),          -- 篮球陪练, 专家
(4, 27, 3, '有健身教练经验');                     -- 健身指导, 精通

-- 张三(id=2) 也帮取快递
INSERT INTO `dbs_user_skill` (`user_id`, `skill_tag_id`, `proficiency`, `description`) VALUES
(2, 31, 1, '顺路帮取快递');                      -- 取快递, 了解

-- ==================== 6. 测试数据：技能货架 ====================
INSERT INTO `dbs_skill_shelf` (`user_id`, `skill_tag_id`, `title`, `description`, `point_price`, `duration_minutes`, `location_type`) VALUES
(2, 16, 'Python编程辅导', '手把手教你Python编程，从入门到爬虫实战', 150, 60, 1),
(2, 18, '前端页面开发', '帮你写Vue/React页面，响应式布局', 200, 120, 1),
(3, 11, '海报设计', '专业级海报设计，含3次修改', 180, 90, 3),
(3, 14, 'PPT美化', '让你的PPT瞬间变高级', 100, 60, 1),
(4, 26, '篮球1v1陪练', '校队主力陪练，可教投篮和运球技巧', 120, 60, 2);

-- ==================== 7. 测试数据：需求 ====================
INSERT INTO `dbs_demand` (`user_id`, `skill_tag_id`, `title`, `description`, `point_reward`, `deadline`, `location_type`, `campus`, `building`, `status`, `demand_type`) VALUES
(3, 17, '急！Java大作业debug', 'Spring Boot项目有个bug怎么都改不好，求助Java大佬', 200, '2026-07-10 23:59:59', 1, '主校区', '2号宿舍楼', 1, 1),
(4, 11, '帮我做个社团招新海报', '篮球社招新需要一张帅气海报', 150, '2026-07-05 23:59:59', 1, '东校区', '3号宿舍楼', 1, 1),
(2, 27, '求健身指导', '想增肌但不会练，找个健身大佬带带我', 100, '2026-07-15 23:59:59', 3, '主校区', '1号宿舍楼', 1, 1),
(3, 31, '明天帮忙取个快递', '有个大件快递，有偿求帮取', 30, '2026-07-01 18:00:00', 2, '主校区', '2号宿舍楼', 1, 1);

-- ========== ROLLBACK ==========
-- 回滚步骤（按依赖顺序-反向）:
-- DELETE FROM `dbs_skill_shelf` WHERE id BETWEEN 1 AND 5;
-- DELETE FROM `dbs_demand` WHERE id BETWEEN 1 AND 4;
-- DELETE FROM `dbs_user_skill` WHERE id BETWEEN 1 AND 9;
-- DELETE FROM `dbs_skill_tag` WHERE id BETWEEN 1 AND 35;
-- DELETE FROM `dbs_skill_category` WHERE id BETWEEN 1 AND 7;
-- DELETE FROM `dbs_user` WHERE username IN ('admin', 'zhangsan', 'lisi', 'wangwu');
