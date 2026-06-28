# 搭把手项目结构规划文档

## 一、项目概述

### 1.1 项目名称
搭把手 (DaBaShou) - 校园共享技能互助平台

### 1.2 项目定位
将青年群体的"闲置技能"与"碎片时间"进行数字化撮合，倡导"技能换时间、时间换服务"的无压力青年互助生态。

### 1.3 技术栈
- **后端**: Spring Boot 4.x, Java 17, MyBatis-Plus, Redis, WebSocket
- **前端**: Vue 3, Element Plus, Vite, TypeScript, Pinia
- **数据库**: MySQL 8.0
- **缓存**: Redis
- **即时通讯**: WebSocket
- **部署**: Docker, Docker Compose

## 二、项目目录结构

### 2.1 完整目录结构
```
DaBaShou/
├── backend/                            # 后端服务
│   ├── dabashou-common/                # 公共模块
│   │   ├── src/main/java/com/dabashou/common/
│   │   │   ├── config/                 # 配置类
│   │   │   ├── constant/               # 常量定义
│   │   │   ├── enums/                  # 枚举类
│   │   │   ├── exception/              # 异常处理
│   │   │   ├── utils/                  # 工具类
│   │   │   └── core/                   # 核心组件
│   │   └── pom.xml
│   ├── dabashou-system/                # 系统管理模块
│   │   ├── src/main/java/com/dabashou/system/
│   │   │   ├── controller/             # 系统管理接口
│   │   │   ├── service/                # 系统管理服务
│   │   │   ├── mapper/                 # 数据访问
│   │   │   └── domain/                 # 实体类
│   │   └── pom.xml
│   ├── dabashou-user/                  # 用户模块
│   │   ├── src/main/java/com/dabashou/user/
│   │   │   ├── controller/             # 用户接口
│   │   │   ├── service/                # 用户服务
│   │   │   ├── mapper/                 # 数据访问
│   │   │   └── domain/                 # 实体类
│   │   └── pom.xml
│   ├── dabashou-skill/                 # 技能模块
│   │   ├── src/main/java/com/dabashou/skill/
│   │   │   ├── controller/             # 技能接口
│   │   │   ├── service/                # 技能服务
│   │   │   ├── mapper/                 # 数据访问
│   │   │   └── domain/                 # 实体类
│   │   └── pom.xml
│   ├── dabashou-shelf/                 # 技能货架模块
│   │   ├── src/main/java/com/dabashou/shelf/
│   │   │   ├── controller/             # 货架接口
│   │   │   ├── service/                # 货架服务
│   │   │   ├── mapper/                 # 数据访问
│   │   │   └── domain/                 # 实体类
│   │   └── pom.xml
│   ├── dabashou-demand/                # 需求模块
│   │   ├── src/main/java/com/dabashou/demand/
│   │   │   ├── controller/             # 需求接口
│   │   │   ├── service/                # 需求服务
│   │   │   ├── mapper/                 # 数据访问
│   │   │   └── domain/                 # 实体类
│   │   └── pom.xml
│   ├── dabashou-order/                 # 订单模块
│   │   ├── src/main/java/com/dabashou/order/
│   │   │   ├── controller/             # 订单接口
│   │   │   ├── service/                # 订单服务
│   │   │   ├── mapper/                 # 数据访问
│   │   │   ├── domain/                 # 实体类
│   │   │   └── statemachine/           # 状态机实现
│   │   └── pom.xml
│   ├── dabashou-point/                 # 积分模块
│   │   ├── src/main/java/com/dabashou/point/
│   │   │   ├── controller/             # 积分接口
│   │   │   ├── service/                # 积分服务
│   │   │   ├── mapper/                 # 数据访问
│   │   │   └── domain/                 # 实体类
│   │   └── pom.xml
│   ├── dabashou-message/               # 消息模块
│   │   ├── src/main/java/com/dabashou/message/
│   │   │   ├── controller/             # 消息接口
│   │   │   ├── service/                # 消息服务
│   │   │   ├── websocket/              # WebSocket处理
│   │   │   ├── mapper/                 # 数据访问
│   │   │   └── domain/                 # 实体类
│   │   └── pom.xml
│   ├── dabashou-credit/                # 信用评价模块
│   │   ├── src/main/java/com/dabashou/credit/
│   │   │   ├── controller/             # 信用评价接口
│   │   │   ├── service/                # 信用评价服务
│   │   │   ├── mapper/                 # 数据访问
│   │   │   └── domain/                 # 实体类
│   │   └── pom.xml
│   ├── dabashou-stat/                  # 数据统计模块
│   │   ├── src/main/java/com/dabashou/stat/
│   │   │   ├── controller/             # 统计接口
│   │   │   ├── service/                # 统计服务
│   │   │   ├── mapper/                 # 数据访问
│   │   │   └── domain/                 # 实体类
│   │   └── pom.xml
│   ├── dabashou-admin/                 # 管理后台模块
│   │   ├── src/main/java/com/dabashou/admin/
│   │   │   ├── controller/             # 管理接口
│   │   │   ├── service/                # 管理服务
│   │   │   └── domain/                 # 实体类
│   │   └── pom.xml
│   ├── dabashou-api/                   # API网关模块
│   │   ├── src/main/java/com/dabashou/api/
│   │   │   └── DabashouApplication.java
│   │   └── pom.xml
│   └── pom.xml                         # 父POM
├── frontend/                           # 前端应用
│   ├── src/
│   │   ├── api/                        # API接口定义
│   │   │   ├── user.ts                 # 用户相关API
│   │   │   ├── skill.ts                # 技能相关API
│   │   │   ├── shelf.ts                # 货架相关API
│   │   │   ├── demand.ts               # 需求相关API
│   │   │   ├── order.ts                # 订单相关API
│   │   │   ├── point.ts                # 积分相关API
│   │   │   ├── message.ts              # 消息相关API
│   │   │   ├── credit.ts               # 信用相关API
│   │   │   └── stat.ts                 # 统计相关API
│   │   ├── assets/                     # 静态资源
│   │   │   ├── images/                 # 图片资源
│   │   │   ├── icons/                  # 图标资源
│   │   │   └── fonts/                  # 字体资源
│   │   ├── components/                 # 公共组件
│   │   │   ├── layout/                 # 布局组件
│   │   │   │   ├── Header.vue          # 头部组件
│   │   │   │   ├── Sidebar.vue         # 侧边栏组件
│   │   │   │   └── Footer.vue          # 底部组件
│   │   │   ├── common/                 # 通用组件
│   │   │   │   ├── DataTable.vue       # 数据表格
│   │   │   │   ├── SearchForm.vue      # 搜索表单
│   │   │   │   └── Pagination.vue      # 分页组件
│   │   │   └── business/               # 业务组件
│   │   │       ├── SkillCard.vue       # 技能卡片
│   │   │       ├── DemandCard.vue      # 需求卡片
│   │   │       └── OrderCard.vue       # 订单卡片
│   │   ├── composables/                # 组合式函数
│   │   │   ├── useUser.ts              # 用户相关
│   │   │   ├── useSkill.ts             # 技能相关
│   │   │   ├── useOrder.ts             # 订单相关
│   │   │   └── useWebSocket.ts         # WebSocket相关
│   │   ├── router/                     # 路由配置
│   │   │   ├── index.ts                # 路由入口
│   │   │   ├── modules/                # 路由模块
│   │   │   │   ├── home.ts             # 首页路由
│   │   │   │   ├── skill.ts            # 技能路由
│   │   │   │   ├── demand.ts           # 需求路由
│   │   │   │   ├── order.ts            # 订单路由
│   │   │   │   ├── user.ts             # 用户路由
│   │   │   │   └── admin.ts            # 管理路由
│   │   │   └── guard.ts                # 路由守卫
│   │   ├── stores/                     # 状态管理
│   │   │   ├── user.ts                 # 用户状态
│   │   │   ├── skill.ts                # 技能状态
│   │   │   ├── order.ts                # 订单状态
│   │   │   ├── message.ts              # 消息状态
│   │   │   └── index.ts                # 状态入口
│   │   ├── styles/                     # 样式文件
│   │   │   ├── variables.scss          # 变量定义
│   │   │   ├── mixins.scss             # 混入
│   │   │   ├── global.scss             # 全局样式
│   │   │   └── element.scss            # Element样式覆盖
│   │   ├── types/                      # TypeScript类型
│   │   │   ├── user.ts                 # 用户类型
│   │   │   ├── skill.ts                # 技能类型
│   │   │   ├── order.ts                # 订单类型
│   │   │   └── api.ts                  # API类型
│   │   ├── utils/                      # 工具函数
│   │   │   ├── request.ts              # 请求封装
│   │   │   ├── auth.ts                 # 认证工具
│   │   │   ├── storage.ts              # 存储工具
│   │   │   └── format.ts               # 格式化工具
│   │   └── views/                      # 页面组件
│   │       ├── home/                   # 首页
│   │       │   ├── index.vue           # 首页
│   │       │   └── components/         # 首页组件
│   │       ├── skill/                  # 技能相关
│   │       │   ├── list.vue            # 技能列表
│   │       │   ├── detail.vue          # 技能详情
│   │       │   └── publish.vue         # 技能发布
│   │       ├── demand/                 # 需求相关
│   │       │   ├── list.vue            # 需求列表
│   │       │   ├── detail.vue          # 需求详情
│   │       │   └── publish.vue         # 需求发布
│   │       ├── order/                  # 订单相关
│   │       │   ├── list.vue            # 订单列表
│   │       │   ├── detail.vue          # 订单详情
│   │       │   └── verify.vue          # 订单核销
│   │       ├── user/                   # 用户中心
│   │       │   ├── profile.vue         # 个人资料
│   │       │   ├── shop.vue            # 技能小铺
│   │       │   ├── points.vue          # 积分管理
│   │       │   └── trust.vue           # 信任分
│   │       ├── message/                # 消息中心
│   │       │   ├── list.vue            # 消息列表
│   │       │   └── chat.vue            # 聊天页面
│   │       ├── credit/                 # 信用评价
│   │       │   ├── list.vue            # 评价列表
│   │       │   └── appeal.vue          # 申诉页面
│   │       ├── stat/                   # 数据统计
│   │       │   ├── overview.vue        # 统计概览
│   │       │   └── analysis.vue        # 统计分析
│   │       └── admin/                  # 管理后台
│   │           ├── system/             # 系统管理
│   │           ├── user/               # 用户管理
│   │           ├── order/              # 订单管理
│   │           ├── credit/             # 信用管理
│   │           └── stat/               # 统计分析
│   ├── public/                         # 公共静态资源
│   ├── index.html                      # HTML模板
│   ├── vite.config.ts                  # Vite配置
│   ├── tsconfig.json                   # TypeScript配置
│   ├── package.json                    # 项目配置
│   └── tailwind.config.js              # Tailwind配置
├── database/                           # 数据库脚本
│   ├── init.sql                        # 初始化脚本
│   └── migration/                      # 数据库迁移脚本
│       ├── V1.0.0__init_schema.sql     # 初始表结构
│       ├── V1.1.0__add_system_tables.sql
│       └── V1.2.0__add_credit_tables.sql
├── docs/                               # 项目文档
│   ├── api/                            # API文档
│   │   └── global-api.md               # 全局API文档
│   ├── design/                         # 设计文档
│   │   ├── database-design.md          # 数据库设计
│   │   └── order-state-machine.md      # 状态机设计
│   ├── project-structure.md            # 项目结构规划
│   └── development-guide.md            # 开发指南
├── docker/                             # Docker配置
│   ├── docker-compose.yml              # Docker Compose配置
│   ├── Dockerfile.backend              # 后端Dockerfile
│   └── Dockerfile.frontend             # 前端Dockerfile
├── scripts/                            # 脚本文件
│   ├── start.sh                        # 启动脚本
│   ├── stop.sh                         # 停止脚本
│   └── deploy.sh                       # 部署脚本
├── AGENTS.md                           # 协作约束文件
└── README.md                           # 项目说明
```

## 三、模块职责说明

### 3.1 后端模块职责

#### dabashou-common (公共模块)
**职责**：提供公共工具类、常量、异常处理、核心组件
**包含**：
- 配置类（Redis、WebSocket、MyBatis等）
- 常量定义（错误码、业务常量）
- 枚举类（订单状态、用户状态等）
- 异常处理（业务异常、系统异常）
- 工具类（日期、字符串、加密等）
- 核心组件（Redis工具、WebSocket工具）

**约束**：
- 禁止包含业务逻辑
- 禁止依赖其他业务模块
- 只能被其他模块依赖

#### dabashou-system (系统管理模块)
**职责**：系统管理、权限控制、文件管理、日志管理
**包含**：
- 角色管理（CRUD、权限分配）
- 权限管理（菜单、按钮权限）
- 文件管理（上传、下载、预览）
- 日志管理（登录日志、操作日志）
- 系统配置（参数配置、字典管理）

**约束**：
- 只有管理员可访问
- 敏感操作必须记录日志
- 配置变更必须生效

#### dabashou-user (用户模块)
**职责**：用户注册、登录、个人信息管理、校园认证
**包含**：
- 用户注册（手机号、邮箱注册）
- 用户登录（密码登录、验证码登录）
- 个人信息管理（资料修改、头像上传）
- 校园认证（学号认证、学生证认证）
- 信任分管理（分数查询、等级展示）

**约束**：
- 密码必须加密存储
- 认证信息必须脱敏
- 信任分计算必须准确

#### dabashou-skill (技能模块)
**职责**：技能分类、标签管理、用户技能管理
**包含**：
- 技能分类管理（CRUD、排序）
- 技能标签管理（CRUD、分类）
- 用户技能管理（添加、删除、修改）

**约束**：
- 分类和标签必须预定义
- 用户技能必须关联标签
- 技能必须有熟练度

#### dabashou-shelf (技能货架模块)
**职责**：技能服务发布、管理、浏览
**包含**：
- 服务发布（标题、描述、价格、时间）
- 服务管理（编辑、下架、删除）
- 服务浏览（列表、详情、搜索）
- 服务推荐（热度、距离、信任分）

**约束**：
- 服务必须关联技能
- 价格必须为正整数
- 服务必须有状态管理

#### dabashou-demand (需求模块)
**职责**：需求发布、匹配、管理
**包含**：
- 需求发布（标题、描述、悬赏、截止时间）
- 需求匹配（地理位置、技能标签）
- 需求管理（编辑、关闭、删除）
- 需求看板（列表、详情、筛选）

**约束**：
- 需求必须有悬赏积分
- 匹配必须基于规则
- 需求必须有状态管理

#### dabashou-order (订单模块)
**职责**：订单状态机、核销码、担保锁单
**包含**：
- 订单创建（从需求或货架创建）
- 状态机流转（待支付→已支付→服务中→待确认→已完成）
- 核销码生成（动态码、时效性）
- 担保锁单（积分冻结、解冻）
- 超时熔断（自动取消、自动退款）

**约束**：
- 状态不可跳变
- 积分操作必须事务
- 核销码必须防伪

#### dabashou-point (积分模块)
**职责**：积分账户管理、积分流水、担保池
**包含**：
- 积分余额查询（可用、冻结）
- 积分明细查询（收入、支出、冻结、解冻）
- 积分获取（注册赠送、任务奖励、好评加分）
- 积分消耗（发布需求、服务结算）
- 担保池管理（冻结、解冻、结算）

**约束**：
- 余额不能为负
- 冻结不能超额
- 流水必须记录

#### dabashou-message (消息模块)
**职责**：即时通讯、系统通知、消息管理
**包含**：
- 即时通讯（用户间聊天）
- 系统通知（订单通知、评价通知）
- 消息管理（已读、删除、清空）
- WebSocket管理（连接、断开、重连）

**约束**：
- 消息必须持久化
- 通知必须实时推送
- WebSocket必须心跳检测

#### dabashou-credit (信用评价模块)
**职责**：双向评价、违规记录、申诉管理
**包含**：
- 双向评价（星级、文字、图片）
- 违规记录（迟到、放鸽子、差评）
- 申诉管理（申诉提交、审核、处理）
- 信任分调整（自动加减分、信用修复）

**约束**：
- 评价必须基于真实交易
- 评价不可修改
- 违规必须记录

#### dabashou-stat (数据统计模块)
**职责**：供需统计、技能热度、用户活跃度
**包含**：
- 供需统计（发布数、接单率、完成率）
- 技能热度（热门技能、类别占比）
- 用户活跃度（活跃用户、信任分分布）
- 统计图表（折线图、柱状图、饼图）

**约束**：
- 统计必须准确
- 图表必须直观
- 数据必须可导出

#### dabashou-admin (管理后台模块)
**职责**：管理后台接口、数据管理、系统监控
**包含**：
- 用户管理（列表、详情、禁用）
- 订单管理（列表、详情、处理）
- 信用管理（评价审核、违规处理）
- 统计分析（数据概览、趋势分析）

**约束**：
- 只有管理员可访问
- 敏感操作必须二次确认
- 操作必须记录日志

#### dabashou-api (API网关模块)
**职责**：聚合所有接口、统一入口
**包含**：
- 应用启动类
- 接口聚合
- 跨域配置
- 异常处理

**约束**：
- 只负责聚合，不包含业务逻辑
- 统一响应格式
- 统一错误处理

### 3.2 前端模块职责

#### api (API接口)
**职责**：定义所有API接口
**包含**：
- 用户相关API
- 技能相关API
- 需求相关API
- 订单相关API
- 积分相关API
- 消息相关API
- 信用相关API
- 统计相关API

**约束**：
- 必须使用TypeScript
- 必须定义请求和响应类型
- 必须统一错误处理

#### components (公共组件)
**职责**：提供可复用的UI组件
**包含**：
- 布局组件（Header、Sidebar、Footer）
- 通用组件（DataTable、SearchForm、Pagination）
- 业务组件（SkillCard、DemandCard、OrderCard）

**约束**：
- 必须可复用
- 必须有Props定义
- 必须有事件定义

#### views (页面组件)
**职责**：实现具体页面功能
**包含**：
- 首页
- 技能相关页面
- 需求相关页面
- 订单相关页面
- 用户中心
- 消息中心
- 信用评价
- 数据统计
- 管理后台

**约束**：
- 必须使用组合式API
- 必须使用TypeScript
- 必须有路由配置

## 四、数据库表结构

### 4.1 核心业务表

#### 用户相关
- `dbs_user` - 用户表
- `user_campus_auth` - 校园认证表
- `user_trust_score_log` - 信任分变动记录表

#### 技能相关
- `dbs_skill_category` - 技能分类表
- `dbs_skill_tag` - 技能标签表
- `dbs_user_skill` - 用户技能表

#### 货架相关
- `dbs_time_slot` - 时间格子表
- `dbs_skill_shelf` - 技能货架表

#### 需求相关
- `dbs_demand` - 需求表

#### 订单相关
- `dbs_order` - 订单表
- `dbs_point_transaction` - 积分流水表

#### 评价相关
- `dbs_review` - 评价表
- `credit_violation` - 违规记录表
- `credit_appeal` - 申诉记录表

### 4.2 系统管理表

#### 权限相关
- `sys_role` - 角色表
- `sys_permission` - 权限表
- `sys_role_permission` - 角色权限关联表
- `sys_user_role` - 用户角色关联表

#### 系统相关
- `sys_file` - 文件表
- `sys_notification` - 通知表
- `sys_log` - 日志表
- `sys_config` - 系统配置表

### 4.3 统计分析表

- `stat_daily_summary` - 每日统计汇总表
- `stat_skill_heat` - 技能热度统计表

## 五、API接口设计

### 5.1 接口规范
- **请求方式**：GET（查询）、POST（创建）、PUT（更新）、DELETE（删除）
- **响应格式**：统一使用`AjaxResult`格式
- **错误码**：使用预定义的错误码
- **认证方式**：JWT Token

### 5.2 接口模块

#### 用户模块
- `POST /api/user/register` - 用户注册
- `POST /api/user/login` - 用户登录
- `GET /api/user/profile` - 获取个人信息
- `PUT /api/user/profile` - 更新个人信息
- `POST /api/user/campus-auth` - 校园认证

#### 技能模块
- `GET /api/skill/categories` - 获取技能分类
- `GET /api/skill/tags` - 获取技能标签
- `GET /api/user/skills` - 获取用户技能
- `POST /api/user/skills` - 添加用户技能

#### 货架模块
- `POST /api/shelf` - 发布技能服务
- `GET /api/shelf/list` - 获取技能货架列表
- `GET /api/shelf/{id}` - 获取技能服务详情
- `PUT /api/shelf/{id}` - 更新技能服务
- `DELETE /api/shelf/{id}` - 删除技能服务

#### 需求模块
- `POST /api/demand` - 发布需求
- `GET /api/demand/list` - 获取需求列表
- `GET /api/demand/{id}` - 获取需求详情
- `PUT /api/demand/{id}` - 更新需求
- `DELETE /api/demand/{id}` - 删除需求

#### 订单模块
- `POST /api/order` - 创建订单
- `GET /api/order/list` - 获取订单列表
- `GET /api/order/{id}` - 获取订单详情
- `POST /api/order/{id}/pay` - 支付订单
- `POST /api/order/{id}/verify` - 核销订单
- `POST /api/order/{id}/cancel` - 取消订单

#### 积分模块
- `GET /api/point/balance` - 获取积分余额
- `GET /api/point/transactions` - 获取积分流水
- `POST /api/point/recharge` - 充值积分

#### 消息模块
- `GET /api/message/list` - 获取消息列表
- `GET /api/message/unread` - 获取未读消息
- `POST /api/message/read` - 标记已读
- `WebSocket /ws/message` - 即时通讯

#### 信用模块
- `POST /api/credit/review` - 提交评价
- `GET /api/credit/reviews` - 获取评价列表
- `POST /api/credit/appeal` - 提交申诉
- `GET /api/credit/appeals` - 获取申诉列表

#### 统计模块
- `GET /api/stat/overview` - 获取统计概览
- `GET /api/stat/demand` - 获取需求统计
- `GET /api/stat/skill` - 获取技能统计
- `GET /api/stat/user` - 获取用户统计

## 六、开发计划

### 6.1 第一阶段：基础框架搭建（2-3周）
1. 初始化后端Spring Boot项目
2. 初始化前端Vue 3项目
3. 搭建开发环境（MySQL、Redis）
4. 实现系统基础通用模块
5. 实现用户与身份认证模块

### 6.2 第二阶段：核心业务开发（4-5周）
1. 实现技能货架与闲时格子发布模块
2. 实现悬赏求助看板与智能匹配模块
3. 实现担保履约与积分核销结算模块
4. 实现互助积分与代币经济模块

### 6.3 第三阶段：信用与统计（2-3周）
1. 实现信用评价与违规管理模块
2. 实现数据统计可视化模块
3. 实现消息通知模块
4. 实现即时通讯功能

### 6.4 第四阶段：管理后台与优化（2周）
1. 实现管理后台
2. 性能优化
3. 安全加固
4. 测试与调试

## 七、部署方案

### 7.1 本地开发
```bash
# 启动数据库
docker-compose -f docker/docker-compose.yml up -d mysql redis

# 启动后端
cd backend
mvn spring-boot:run

# 启动前端
cd frontend
npm install
npm run dev
```

### 7.2 生产部署
```bash
# 构建镜像
docker-compose -f docker/docker-compose.yml build

# 启动服务
docker-compose -f docker/docker-compose.yml up -d
```

## 八、注意事项

### 8.1 开发规范
- 遵循AGENTS.md中的约束规则
- 修改代码前必须先阅读相关文档
- 新增功能必须同步更新API文档
- 重要业务逻辑必须编写单元测试

### 8.2 协作规范
- 使用Git进行版本控制
- 遵循分支管理规范
- 代码必须经过审查
- 提交信息必须规范

### 8.3 安全规范
- 密码必须加密存储
- 敏感信息必须脱敏
- 接口必须进行认证
- 操作必须记录日志

---
**文档版本**：v1.0.0
**最后更新**：2026年6月28日