# 搭把手数据库设计

## 核心业务表

### 1. 用户表 (dbs_user)
- 用户基础信息
- 积分余额
- 信任分
- 地理位置信息

### 2. 技能分类表 (dbs_skill_category)
- 技能分类体系

### 3. 技能标签表 (dbs_skill_tag)
- 具体技能标签

### 4. 用户技能表 (dbs_user_skill)
- 用户拥有的技能关联

### 5. 时间格子表 (dbs_time_slot)
- 用户可预约的时间段

### 6. 技能货架表 (dbs_skill_shelf)
- 用户上架的技能服务

### 7. 需求表 (dbs_demand)
- 悬赏求助信息

### 8. 订单表 (dbs_order)
- 服务订单（状态机管理）

### 9. 积分流水表 (dbs_point_transaction)
- 积分变动记录

### 10. 评价表 (dbs_review)
- 服务评价

## 系统管理表

### 11. 角色表 (sys_role)
- 系统角色定义

### 12. 权限表 (sys_permission)
- 菜单和按钮权限

### 13. 文件表 (sys_file)
- 文件管理

### 14. 通知表 (sys_notification)
- 系统通知

### 15. 日志表 (sys_log)
- 操作日志

### 16. 系统配置表 (sys_config)
- 系统参数配置

## 信用评价表

### 17. 校园认证表 (user_campus_auth)
- 校园身份认证

### 18. 信任分变动记录表 (user_trust_score_log)
- 信任分变动历史

### 19. 违规记录表 (credit_violation)
- 违规行为记录

### 20. 申诉记录表 (credit_appeal)
- 申诉处理记录

## 统计分析表

### 21. 每日统计汇总表 (stat_daily_summary)
- 平台每日数据统计

### 22. 技能热度统计表 (stat_skill_heat)
- 技能热度分析