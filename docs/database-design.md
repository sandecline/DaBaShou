# 数据库设计文档

> 搭把手 (DaBaShou) — 校园技能共享平台

## 概述

- 数据库: MySQL 8.0+
- 字符集: utf8mb4
- 引擎: InnoDB
- 迁移工具: Flyway (11 个版本脚本: V1.0.0 - V1.10.0)
- 表总数: 28 张 (16 业务表 + 8 系统表 + 4 扩展表)

## 一、用户模块

### dbs_user — 用户表
| 字段 | 类型 | 说明 |
|---|---|---|
| id | BIGINT PK | 用户ID |
| username | VARCHAR(50) UK | 用户名 |
| password | VARCHAR(256) | 密码(BCrypt) |
| nickname | VARCHAR(50) | 昵称 |
| avatar | VARCHAR(255) | 头像URL |
| phone | VARCHAR(20) | 手机号 |
| email | VARCHAR(100) | 邮箱 |
| point_balance | INT DEFAULT 100 | 积分余额 |
| trust_score | DECIMAL(2,1) DEFAULT 5.0 | 信任分(0.0-5.0) |
| longitude/latitude | DECIMAL(10,7) | 经纬度 |
| campus/building/bio | VARCHAR | 校区/楼栋/简介 |
| status | TINYINT | 0-禁用 1-正常 |
| create_time/update_time | DATETIME | 审计字段 |

### dbs_user_skill — 用户技能表
| 字段 | 说明 |
|---|---|
| user_id + skill_tag_id | UK(user_id, skill_tag_id) |
| proficiency | TINYINT 1-了解 2-熟悉 3-精通 4-专家 |
| description | VARCHAR(500) |

### user_campus_auth — 校园认证表
| 字段 | 说明 |
|---|---|
| auth_type | 认证类型 |
| student_no | 学号 |
| status | 0-待审核 1-已通过 2-已拒绝 |

## 二、技能模块

### dbs_skill_category — 技能分类表
| 字段 | 说明 |
|---|---|
| name | 分类名称（7类：学业辅导/维修帮忙/设计美工/技术支持/运动陪练/音乐艺术/生活服务） |
| icon | 图标 |
| sort_order | 排序 |

### dbs_skill_tag — 技能标签表
| 字段 | 说明 |
|---|---|
| category_id | FK → dbs_skill_category |
| name | 标签名称（35个标签） |
| status | 0-禁用 1-正常 |

## 三、交易模块

### dbs_skill_shelf — 技能货架表
| 字段 | 说明 |
|---|---|
| user_id | FK → dbs_user，发布者 |
| skill_tag_id | FK → dbs_skill_tag |
| title | 服务标题 |
| description | TEXT 服务描述 |
| point_price | INT 积分价格 |
| duration_minutes | 预计时长 |
| location_type | TINYINT 1-线上 2-线下 3-均可 |
| status | TINYINT 0-下架 1-上架 2-审核中 |
| **索引** | idx_user, idx_skill, idx_status |

### dbs_demand — 需求表
| 字段 | 说明 |
|---|---|
| user_id | FK → dbs_user，发布者 |
| skill_tag_id | 所需技能标签 |
| point_reward | INT 悬赏积分 |
| deadline | DATETIME 截止时间 |
| demand_type | TINYINT 1-求助悬赏 2-技能置换 |
| location_type | TINYINT 1-线上 2-线下 3-均可 |
| status | TINYINT 0-已关闭 1-待接单 2-进行中 3-已完成 |
| **索引** | idx_user, idx_skill, idx_status, idx_status_type(复合), idx_location |

### dbs_time_slot — 时间格子表
| 字段 | 说明 |
|---|---|
| user_id | FK → dbs_user |
| date | DATE |
| start_time/end_time | TIME |
| status | TINYINT 0-不可用 1-可预约 2-已预约 |
| **索引** | idx_user_date |

## 四、订单模块

### dbs_order — 订单表
| 字段 | 类型 | 说明 |
|---|---|---|
| id | BIGINT PK | 订单ID |
| order_no | VARCHAR(32) | 订单号(DBS+时间戳+UUID4) |
| buyer_id/seller_id | BIGINT FK | 买家/卖家 |
| demand_id/skill_shelf_id/skill_tag_id | BIGINT | 关联业务ID |
| title | VARCHAR(100) | 服务标题 |
| point_amount | INT | 积分金额 |
| status | TINYINT | **0-取消 1-待支付 2-已支付 3-服务中 4-待确认 5-已完成 6-已退款 7-争议** |
| verify_code | VARCHAR(6) UK | 核销码(6位数字) |
| verify_code_expire | DATETIME | 核销码过期时间 |
| time_slot_id | BIGINT | 时间格子 |
| remark | VARCHAR(200) | 备注 |
| deleted | TINYINT | 逻辑删除(0/1) |
| **索引** | idx_buyer_status, idx_seller_status(复合), uk_verify_code |

## 五、积分模块

### dbs_point_account — 积分账户表
| 字段 | 说明 |
|---|---|
| user_id | UK，一个用户一个账户 |
| available | 可用积分 |
| frozen | 冻结积分 |
| total_earned/total_spent | 累计收支 |

### dbs_point_transaction — 积分流水表
| 字段 | 说明 |
|---|---|
| user_id/order_id | 用户/关联订单 |
| type | 1-收入 2-支出 3-冻结 4-解冻 5-系统奖励 6-系统扣除 |
| amount | 变动金额 |
| balance_after | 变动后余额 |
| description | 说明 |

### dbs_point_freeze — 冻结记录表
| 字段 | 说明 |
|---|---|
| order_id | UK，一个订单一条冻结 |
| amount | 冻结金额 |
| status | 1-冻结中 2-已解冻 3-已结算 |

### dbs_guarantee_pool — 担保池表
| 字段 | 说明 |
|---|---|
| order_id | UK，订单关联 |
| amount | 担保金额 |
| status | 1-担保中 2-已结算 3-已退还 |

## 六、信用模块

### dbs_review — 评价表
| 字段 | 说明 |
|---|---|
| order_id + reviewer_id | UK(order_id, reviewer_id) |
| rating | TINYINT 1-5 |
| content | VARCHAR(500) |
| images | VARCHAR(1000) JSON数组 |
| is_anonymous | TINYINT 0-否 1-是 |

### credit_violation — 违规记录表
| 字段 | 说明 |
|---|---|
| user_id | 违规用户 |
| type | VARCHAR(30) cheat/spam/fraud/other |
| penalty_score | DECIMAL(3,1) 扣分 |
| reporter_id | 举报人 |
| status | 0-已撤销 1-有效 |

### credit_appeal — 申诉表
| 字段 | 说明 |
|---|---|
| violation_id | FK → credit_violation |
| appellant_id | 申诉人 |
| reason | VARCHAR(500) |
| status | 0-待审核 1-已通过 2-已驳回 |

## 七、消息模块

### dbs_chat_session — 聊天会话表
| 字段 | 说明 |
|---|---|
| user1_id/user2_id | 双方用户 |
| last_message | VARCHAR(500) 最后一条消息 |
| unread_count | INT 未读数 |

### dbs_chat_message — 聊天消息表
| 字段 | 说明 |
|---|---|
| session_id | FK → dbs_chat_session |
| sender_id | FK → dbs_user |
| content | TEXT |
| msg_type | TINYINT 1-文字 2-图片 |
| is_read | TINYINT 0-未读 1-已读 |

### dbs_notification — 系统通知表 (V1.10.0)
| 字段 | 说明 |
|---|---|
| user_id | 接收者 |
| type | system/order/credit/point/chat |
| title/content | 通知标题/内容 |
| related_type/related_id | 关联业务 |
| is_read | 0-未读 1-已读 |

## 八、统计模块

### dbs_stat_daily — 每日统计表
| date | new_user_count | active_user_count | new_order_count | completed_order_count | point_inflow | point_outflow |

### dbs_stat_skill_heat — 技能热度表
| skill_tag_id | shelf_count | demand_count | order_count | heat_score | stat_date |

## 九、系统表

| 表名 | 说明 |
|---|---|
| sys_user | 系统用户 |
| sys_role | 角色 |
| sys_user_role | 用户-角色关联 |
| sys_menu | 菜单 |
| sys_role_menu | 角色-菜单关联 |
| sys_dict_type | 字典类型 |
| sys_dict_data | 字典数据 |
| sys_file | 文件管理 |

## 命名规范
- 表名: dbs_前缀 或 credit_前缀（小写下划线）
- 字段: 小写下划线（user_id, create_time）
- 主键: id BIGINT AUTO_INCREMENT
- 外键: 引用表名_id
- 索引: idx_字段名

## 迁移脚本版本
| 版本 | 内容 |
|---|---|
| V1.0.0 | 核心表 + 系统表 |
| V1.1.0 | 补充表 |
| V1.2.0 | 信用表(credit_violation/credit_appeal) |
| V1.4.0 | 聊天表 + demand_type |
| V1.5.0 | 积分三表(point_account/freeze/guarantee_pool) |
| V1.6.0 | 索引优化 + verify_code UK + Order.deleted |
| V1.7.0 | 种子数据(管理员/分类/标签/测试用户) |
| V1.8.0 | Order.remark |
| V1.9.0 | Review.images/is_anonymous |
| V1.10.0 | dbs_notification |
| V1.11.0 | 积分流水索引优化 |
| V1.12.0 | 测试数据(20用户+订单+积分+评价+聊天+统计) |
