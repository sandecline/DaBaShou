# 搭把手 (DaBaShou)

校园共享技能互助平台

## 项目定位

将青年群体的"闲置技能"与"碎片时间"进行数字化撮合，倡导"技能换时间、时间换服务"的无压力青年互助生态。

## 核心业务线

1. **技能货架与闲时格子发布线**：用户可上架个人非全职技能，并能在可视化的个人日历矩阵（时间格子）中勾选本周空闲时段，生成个人专属的技能小铺。

2. **悬赏求助看板与智能匹配线**：提供公共求助墙，用户可发布即时性、碎片化需求。系统需根据地理位置（如同校区、同楼栋）与技能标签进行供需的双向匹配。

3. **担保履约与积分核销结算线**：用户揭榜接单后，系统自动冻结买方积分进入第三方担保池。线下交付时，通过动态核销码扫码确认，积分即时解冻结算，并更新双方履约信任分。

## 技术栈

- **后端**: Spring Boot 4.x, Java  21, MyBatis-Plus, Redis, WebSocket
- **前端**: Vue 3, Element Plus, Vite, TypeScript, Pinia
- **数据库**: MySQL 8.0
- **缓存**: Redis
- **即时通讯**: WebSocket
- **部署**: Docker, Docker Compose

## 功能模块清单

### 一、系统基础通用模块
- 用户权限管理（学生、管理员、客服审核员）
- 系统基础功能（文件管理、消息通知、日志管理、系统参数配置）

### 二、用户与身份认证模块
- 校园身份认证（学号、校园邮箱、学生证）
- 个人技能小铺主页
- 信任分体系（新人/靠谱/金牌）

### 三、技能货架与闲时格子发布模块
- 技能上架管理（发布、编辑、下架）
- 闲时格子日历（可视化时间矩阵）
- 技能标签与分类
- 技能浏览与搜索

### 四、悬赏求助看板与智能匹配模块
- 求助发布管理（即时需求、急单悬赏）
- 需求看板展示（公共求助墙）
- 智能供需匹配（地理位置、技能标签）
- 接单管理（揭榜接单、接单率统计）

### 五、担保履约与积分核销结算模块
- 状态机流程控制（已发布→已接单→待履约→待核销→已完成）
- 担保锁单（积分冻结、反悔处理）
- 动态核销码（扫码核销、时效防伪）
- 熔断与退改规则

### 六、互助积分与代币经济模块
- 积分账户管理（余额查询、流水明细）
- 积分获取与消耗（注册赠送、任务奖励、好评加分）
- 置换需求管理（技能换技能）

### 七、数据统计可视化模块
- 供需数据统计（发布数、接单率、完成率）
- 技能热度统计（热门技能排行）
- 用户活跃度统计（活跃用户、信任分分布）

### 八、信用评价与违规管理模块
- 双向评价体系（星级打分、文字评价）
- 违规记录存档（迟到、放鸽子、差评）
- 信任分动态调整（自动加减分、信用修复）

### 九、系统优化与技术适配
- 数据安全防护（加密、脱敏、备份）
- 流程自动化（归档、关闭、审核）
- 移动端适配（响应式H5）
- 技术适配优化（分页、批量操作）

## 项目结构

```
DaBaShou/
├── backend/                    # 后端服务
│   ├── dabashou-common/        # 公共模块
│   ├── dabashou-system/        # 系统管理模块
│   ├── dabashou-user/          # 用户模块
│   ├── dabashou-skill/         # 技能模块
│   ├── dabashou-shelf/         # 技能货架模块
│   ├── dabashou-demand/        # 需求模块
│   ├── dabashou-order/         # 订单模块
│   ├── dabashou-point/         # 积分模块
│   ├── dabashou-message/       # 消息模块
│   ├── dabashou-credit/        # 信用评价模块
│   ├── dabashou-stat/          # 数据统计模块
│   ├── dabashou-admin/         # 管理后台模块
│   └── dabashou-api/           # API网关模块
├── frontend/                   # 前端应用
│   ├── src/
│   │   ├── api/                # API接口
│   │   ├── assets/             # 静态资源
│   │   ├── components/         # 公共组件
│   │   ├── composables/        # 组合式函数
│   │   ├── router/             # 路由配置
│   │   ├── stores/             # 状态管理
│   │   ├── styles/             # 样式文件
│   │   ├── types/              # TypeScript类型
│   │   ├── utils/              # 工具函数
│   │   └── views/              # 页面组件
│   │       ├── home/           # 首页
│   │       ├── skill/          # 技能相关
│   │       ├── demand/         # 需求相关
│   │       ├── order/          # 订单相关
│   │       ├── user/           # 用户中心
│   │       ├── message/        # 消息中心
│   │       ├── credit/         # 信用评价
│   │       ├── stat/           # 数据统计
│   │       └── admin/          # 管理后台
│   ├── public/                 # 公共静态资源
│   ├── index.html              # HTML模板
│   ├── vite.config.ts          # Vite配置
│   ├── tsconfig.json           # TypeScript配置
│   ├── package.json            # 项目配置
│   └── tailwind.config.js      # Tailwind配置
├── database/                   # 数据库脚本
│   ├── init.sql                # 初始化脚本
│   └── migration/              # 数据库迁移脚本
├── docs/                       # 项目文档
│   ├── api/                    # API文档
│   ├── design/                 # 设计文档
│   ├── database-design.md      # 数据库设计
│   └── order-state-machine.md  # 状态机设计
├── docker/                     # Docker配置
│   ├── docker-compose.yml      # Docker Compose配置
│   ├── Dockerfile.backend      # 后端Dockerfile
│   └── Dockerfile.frontend     # 前端Dockerfile
├── AGENTS.md                   # 协作约束文件
└── README.md                   # 项目说明
```

## 数据库设计

### 核心表结构
- `dbs_user` - 用户表
- `dbs_skill_category` - 技能分类表
- `dbs_skill_tag` - 技能标签表
- `dbs_user_skill` - 用户技能表
- `dbs_time_slot` - 时间格子表
- `dbs_skill_shelf` - 技能货架表
- `dbs_demand` - 需求表
- `dbs_order` - 订单表（状态机管理）
- `dbs_point_transaction` - 积分流水表
- `dbs_review` - 评价表

### 系统管理表
- `sys_role` - 角色表
- `sys_permission` - 权限表
- `sys_file` - 文件表
- `sys_notification` - 通知表
- `sys_log` - 日志表
- `sys_config` - 系统配置表

### 信用评价表
- `user_campus_auth` - 校园认证表
- `user_trust_score_log` - 信任分变动记录表
- `credit_violation` - 违规记录表
- `credit_appeal` - 申诉记录表

### 统计分析表
- `stat_daily_summary` - 每日统计汇总表
- `stat_skill_heat` - 技能热度统计表

## 订单状态机

```
待支付(1) → 已支付(2) → 服务中(3) → 待确认(4) → 已完成(5)
    ↓           ↓           ↓           ↓
  已取消(0)   已退款(6)   已退款(6)   争议中(7)
                ↓           ↓           ↓
            已取消(0)    已取消(0)    已完成(5)/已退款(6)
```

### 关键业务规则
- **支付环节**：买家支付积分，积分冻结到担保池，生成动态核销码
- **服务确认**：线下服务扫码核销，线上服务买家确认
- **超时熔断**：待支付超时15分钟自动取消，核销码超时30分钟自动退款
- **退改扣分**：买家取消扣10%积分，卖家取消扣20%积分

## 开发规范

### 代码规范
- 业务第一：理解每一行代码背后的"本地互助"和"信任履约"场景
- 严谨流转：高度重视积分资产流转的事务一致性和订单状态机硬性流转逻辑
- 极简交互：适合青年群体的、带有社交调性和高颜值的日历看板、需求信息流和卡片化小铺

### 协作规范
- 遵循AGENTS.md中的约束规则
- 修改代码前必须先阅读相关文档
- 新增功能必须同步更新API文档
- 重要业务逻辑必须编写单元测试

## 快速开始

### 环境要求
- JDK 17+
- Node.js 18+
- MySQL 8.0+
- Redis 7.0+
- Docker & Docker Compose (可选)

### 本地开发
```bash
# 1. 克隆项目
git clone https://github.com/sandecline/DaBaShou.git

# 2. 启动数据库
docker-compose -f docker/docker-compose.yml up -d mysql redis

# 3. 启动后端
cd backend
mvn spring-boot:run

# 4. 启动前端
cd frontend
npm install
npm run dev
```

### Docker部署
```bash
# 一键启动所有服务
docker-compose -f docker/docker-compose.yml up -d
```

## 项目文档

- [需求清单与功能说明](docs/requirements.md) - 面向用户的功能说明
- [项目结构规划](docs/project-structure.md) - 详细的项目结构说明
- [数据库设计文档](docs/design/database-design.md) - 数据库表结构设计
- [订单状态机设计](docs/design/order-state-machine.md) - 订单状态流转规则
- [API接口文档](docs/api/) - 接口文档
- [协作约束文件](AGENTS.md) - 多人协作规范

## 许可证

MIT License