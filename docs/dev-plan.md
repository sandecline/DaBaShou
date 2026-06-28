# 搭把手 开发计划文档

## 一、项目目标与范围

### 1.1 核心定位
搭把手（DaBaShou）是一个校园共享技能互助平台，将青年群体的"闲置技能"与"碎片时间"进行数字化撮合，倡导"技能换时间、时间换服务"的无压力青年互助生态。

### 1.2 三条核心业务线
1. **技能货架与闲时格子发布线**：上架个人非全职技能，可视化日历矩阵勾选空闲时段，生成个人专属技能小铺
2. **悬赏求助看板与智能匹配线**：公共求助墙发布碎片化需求，基于地理位置与技能标签双向匹配
3. **担保履约与积分核销结算线**：积分冻结到担保池，动态核销码扫码确认，积分即时解冻结算并更新信任分

### 1.3 功能模块（9 大模块）
| 序号 | 模块             | 包含子功能                                         |
| ---- | ---------------- | -------------------------------------------------- |
| 1    | 系统基础通用     | 权限管理（学生/管理员/客服）、文件、通知、日志、配置 |
| 2    | 用户与身份认证   | 校园认证（学号/邮箱/学生证）、个人技能小铺、信任分体系 |
| 3    | 技能货架         | 技能上架、闲时格子日历、标签分类、浏览搜索         |
| 4    | 悬赏求助看板     | 需求发布、公共求助墙、智能匹配（地理+技能）、接单管理 |
| 5    | 担保履约结算     | 状态机（8 状态）、担保锁单、动态核销码、熔断退改   |
| 6    | 互助积分         | 账户管理、积分获取/消耗、技能置换                   |
| 7    | 数据统计         | 供需统计、技能热度、用户活跃度、图表可视化         |
| 8    | 信用评价         | 双向评价、违规记录、申诉管理、信任分动态调整       |
| 9    | 管理后台         | 用户/订单/信用管理、系统配置                        |

### 1.4 非目标（不在本项目范围）
- 移动端原生 App（仅做响应式 H5）
- 第三方支付对接（使用平台积分体系）
- 实时音视频通讯（仅文字 + 图片消息）

---

## 二、技术栈与版本锁定

详见 `docs/constraints.md` 第二章。核心版本：
- **后端**：Spring Boot 4.0.x + Java 21 + MyBatis-Plus 3.5.x + Redis 7.x
- **前端**：Vue 3.4+ + TypeScript 5.x + Vite 5.x + Element Plus 2.x + Pinia 2.x + Tailwind CSS 3.x
- **数据库**：MySQL 8.0+（InnoDB）
- **部署**：Docker + Docker Compose

---

## 三、模块开发依赖顺序

```
Phase 0: 骨架搭建（当前阶段）
  ↓
Phase 1: dabashou-common → dabashou-system + dabashou-user
  ↓
Phase 2: dabashou-skill → dabashou-shelf + dabashou-demand → dabashou-order + dabashou-point
  ↓
Phase 3: dabashou-credit + dabashou-stat + dabashou-message
  ↓
Phase 4: dabashou-admin + dabashou-api（聚合）+ 性能优化 + 安全加固
```

---

## 四、第 0 阶段 · 启动期（当前，可立即执行）

> 目标：项目从"文档+空壳"变为"可编译、可运行、可验证"的骨架工程。

### 任务 0.1：仓库与分支策略
**产出物**：
- `develop` 分支从 `main` 创建
- `main` 分支保护规则（禁止直接推送）
- 分支命名规范落地（feature/fix/hotfix）
- PR 模板（`.github/pull_request_template.md`）

**验收标准**：
- `git branch -a` 可见 `main` + `develop`
- `main` 分支保护已配置

**执行步骤**：
1. `git checkout -b develop && git push -u origin develop`
2. 在 GitHub 设置 main 分支保护规则
3. 创建 `.github/pull_request_template.md`

---

### 任务 0.2：后端骨架
**产出物**：
- `backend/pom.xml`（父 POM，声明依赖版本 + 子模块）
- 12 个子模块的 `pom.xml`（各自的依赖声明）
- `dabashou-common` 基础类：
  - `AjaxResult`：统一响应封装
  - `BusinessException`：业务异常
  - `ErrorCode`：统一错误码枚举
  - `OrderStatus`：订单状态枚举
  - `PageQuery` / `PageResult`：分页封装

**验收标准**：
- `mvn clean compile` 在根目录执行成功
- `AjaxResult.success(data)` 可返回标准格式
- 无编译错误、无 WARNING（deprecation 除外）

**执行步骤**：
1. 创建父 `pom.xml`（Spring Boot 3.2.x parent，Java 21，声明子模块）
2. 为每个子模块创建 `pom.xml`（继承父 POM，声明所需依赖）
3. 创建 `DabashouApplication.java`（在 dabashou-api 模块）
4. 在 dabashou-common 中编写基础类（AjaxResult、BusinessException、ErrorCode 等）

---

### 任务 0.3：前端骨架
**产出物**：
- `frontend/package.json`、`vite.config.ts`、`tsconfig.json`、`tailwind.config.js`
- `src/utils/request.ts`（Axios 封装，统一错误拦截）
- `src/router/index.ts` + `src/router/guard.ts`（路由入口 + 守卫）
- `src/components/layout/Header.vue`、`Sidebar.vue`、`Footer.vue`
- `src/stores/user.ts`（用户状态 store 骨架）
- `src/App.vue`、`src/main.ts`

**验收标准**：
- `npm install && npm run dev` 可启动开发服务器
- `npm run lint && npm run typecheck` 通过
- 浏览器访问可见基础布局

**执行步骤**：
1. `npm create vite@latest . -- --template vue-ts`（在 frontend 目录初始化）
2. 配置 `vite.config.ts`（代理 /api 到后端、Element Plus 按需引入）
3. 配置 `tailwind.config.js`、`postcss.config.js`
4. 编写 `request.ts`（Axios 实例 + 请求/响应拦截器）
5. 编写路由和布局组件

---

### 任务 0.4：数据库迁移机制
**产出物**：
- Flyway 配置（`application-dev.yml` 中启用 Flyway）
- 将现有 `init.sql` 拆分为版本化脚本：
  - `V1.0.0__init_core_tables.sql`（用户/技能/货架/需求/订单/积分/评价）
  - `V1.1.0__init_system_tables.sql`（sys_role/sys_permission/sys_file/sys_notification/sys_log/sys_config + 关联表）
  - `V1.2.0__init_credit_tables.sql`（user_campus_auth/user_trust_score_log/credit_violation/credit_appeal）
  - `V1.3.0__init_stat_tables.sql`（stat_daily_summary/stat_skill_heat）
- 原 `init.sql` 保留为参考，标注"已迁移至 Flyway 脚本"

**验收标准**：
- MySQL 容器启动后 Flyway 自动执行全部脚本
- `SHOW TABLES` 可见全部 22 张表
- 所有表结构与 `docs/design/database-design.md` 一致
- 外键、索引、注释完整

**执行步骤**：
1. 在父 POM 添加 Flyway 依赖
2. 在 `application-dev.yml` 配置 Flyway（locations、baseline-on-migrate）
3. 拆分 init.sql 为 4 个版本化脚本
4. 补全缺失的 12 张表的建表语句（参考 database-design.md）
5. 本地启动验证 Flyway 自动迁移

---

### 任务 0.5：容器化
**产出物**：
- `docker/docker-compose.yml`（MySQL 8.0 + Redis 7 + 后端 + 前端）
- `docker/Dockerfile.backend`（多阶段构建：Maven 编译 + JRE 运行）
- `docker/Dockerfile.frontend`（多阶段构建：Node 编译 + Nginx 部署）
- `scripts/start.sh`、`scripts/stop.sh`、`scripts/deploy.sh`

**验收标准**：
- `docker-compose up -d` 一键启动全部服务
- 浏览器访问 `http://localhost` 可见前端页面
- 前端 `/api/**` 请求可代理到后端

**执行步骤**：
1. 编写 `docker-compose.yml`（4 个服务：mysql、redis、backend、frontend）
2. 编写 `Dockerfile.backend`（FROM maven:3.9-eclipse-temurin-21 AS build → FROM eclipse-temurin:21-jre）
3. 编写 `Dockerfile.frontend`（FROM node:20-alpine AS build → FROM nginx:alpine）
4. 编写启动/停止/部署脚本

---

### 任务 0.6：验证链路约定
**产出物**：
- 后端验证命令：`mvn clean verify`（编译 + 测试 + 覆盖率报告）
- 前端验证命令：`npm run lint && npm run typecheck && npm run test`
- 验证命令写入 `AGENTS.md` 和 `docs/constraints.md`

**验收标准**：
- 后端 `mvn clean verify` 全绿
- 前端 `npm run lint && npm run typecheck` 全绿
- CI 命令文档化

---

## 五、第 1 阶段 · 基础框架（2-3 周）

### 目标
实现系统通用模块 + 用户认证模块，打通前后端联调链路。

### 里程碑
| 里程碑          | 交付物                                        | 验收标准                          |
| --------------- | --------------------------------------------- | --------------------------------- |
| M1.1 系统通用   | 角色/权限 CRUD、文件上传、日志记录、系统配置  | 管理员可登录，权限分配生效        |
| M1.2 用户注册登录 | 注册/登录 API + 前端页面、JWT 认证          | 用户可注册、登录、获取 token      |
| M1.3 校园认证   | 学号/邮箱认证 API、信任分初始化               | 认证后显示学校标识                |
| M1.4 个人主页   | 个人资料编辑、技能小铺骨架、前端页面          | 个人主页可访问，展示基本信息      |

### 关键模块
- `dabashou-system`：角色、权限、文件、日志、配置
- `dabashou-user`：注册、登录、认证、信任分

---

## 六、第 2 阶段 · 核心业务（1天）

### 目标
实现技能货架 + 需求看板 + 订单状态机 + 积分担保，打通核心业务闭环。

### 里程碑
| 里程碑          | 交付物                                                    | 验收标准                              |
| --------------- | --------------------------------------------------------- | ------------------------------------- |
| M2.1 技能货架   | 技能发布/编辑/下架、闲时格子日历、标签分类、浏览搜索      | 用户可发布技能，设置空闲时段          |
| M2.2 需求看板   | 需求发布/关闭、公共求助墙、智能匹配推荐                   | 用户可发布需求，系统自动推荐匹配      |
| M2.3 订单状态机 | 8 状态完整流转、超时熔断、退改扣分                        | 订单可正常流转，超时自动处理          |
| M2.4 积分担保   | 积分冻结/解冻/结算、担保池、核销码生成/验证               | 支付冻结积分，核销后结算              |
| M2.5 端到端联调 | 完整业务流程：发布→下单→支付→核销→结算→评价              | 场景 1（PPT 排版帮助）跑通           |

### 关键模块
- `dabashou-skill`：分类、标签、用户技能
- `dabashou-shelf`：技能服务发布管理
- `dabashou-demand`：需求发布、匹配
- `dabashou-order`：状态机（**核心难点**，参见 `docs/design/order-state-machine.md`）
- `dabashou-point`：积分账户、流水、担保池

### 技术难点与对策
| 难点             | 对策                                                             |
| ---------------- | ---------------------------------------------------------------- |
| 状态机一致性     | 状态机引擎（Spring StateMachine 或手写枚举状态机），单元测试全覆盖 |
| 积分并发安全     | Redis 分布式锁 + 数据库乐观锁 + @Transactional                   |
| 核销码防伪       | 6 位随机码 + Redis 存储 + 30 分钟 TTL + 防截图（动态刷新）       |
| 超时熔断         | Spring @Scheduled 定时扫描 + Redis 延迟队列                      |

---

## 七、第 3 阶段 · 信用与统计（2-3天）

### 目标
实现信用评价 + 数据统计 + 消息通讯，完善平台生态。

### 里程碑
| 里程碑          | 交付物                                          | 验收标准                        |
| --------------- | ----------------------------------------------- | ------------------------------- |
| M3.1 双向评价   | 星级+文字评价、评价展示                         | 完成订单后双方可互评            |
| M3.2 信用体系   | 信任分自动调整、违规记录、申诉机制              | 差评自动扣分，可发起申诉        |
| M3.3 消息通知   | 系统通知（接单/核销/评价/超时）、消息管理       | 实时收到订单状态通知            |
| M3.4 即时通讯   | 用户间聊天（WebSocket）、聊天记录               | 买卖双方可在线沟通              |
| M3.5 数据统计   | 供需统计、技能热度、用户活跃度、图表（ECharts） | 管理后台可查看平台数据          |

### 关键模块
- `dabashou-credit`：评价、违规、申诉、信任分
- `dabashou-message`：WebSocket、系统通知、聊天
- `dabashou-stat`：统计分析、图表数据

---

## 八、第 4 阶段 · 管理后台与优化（2 天）

### 目标
完成管理后台，性能优化与安全加固，达到可演示状态。

### 里程碑
| 里程碑          | 交付物                                        | 验收标准                        |
| --------------- | --------------------------------------------- | ------------------------------- |
| M4.1 管理后台   | 用户/订单/信用/统计管理、系统配置              | 管理员可管理所有业务数据        |
| M4.2 性能优化   | SQL 优化、缓存策略、分页优化、接口响应 ≤500ms | 核心接口响应达标                |
| M4.3 安全加固   | 参数校验、防刷机制、风控规则、审计日志         | 通过安全测试                    |
| M4.4 测试收尾   | 单元测试补全、集成测试、E2E 测试               | 覆盖率达标（核心≥80%）          |
| M4.5 部署上线   | Docker Compose 生产配置、部署文档              | 可一键部署                      |

---

## 九、遗留问题修复

以下是启动阶段发现的问题，纳入 Phase 0 或后续阶段一并解决：

| 序号 | 问题                                             | 处置                                          | 阶段 |
| ---- | ------------------------------------------------ | --------------------------------------------- | ---- |
| 1    | init.sql 只含 10/22 张表                         | 补全 12 张缺失表，拆分为 Flyway 版本化脚本    | 0.4  |
| 2    | init.sql 是单体，未按 Flyway 版本化              | 拆分为 V1.0.0 ~ V1.3.0 四个脚本               | 0.4  |
| 3    | docker/ 和 scripts/ 目录为空                     | 补全 docker-compose.yml + Dockerfile + 脚本    | 0.5  |

---

## 十、风险与对策

| 风险                         | 概率 | 影响 | 对策                                                     |
| ---------------------------- | ---- | ---- | -------------------------------------------------------- |                 |
| 状态机实现复杂导致 bug       | 高   | 高   | 手写枚举状态机 + 单元测试覆盖所有合法/非法流转           |
| 积分并发扣减导致余额异常     | 高   | 高   | Redis 分布式锁 + 数据库乐观锁 + 事务回滚                 |
| 前后端联调接口不一致         | 中   | 中   | Knife4j 自动生成 API 文档，前端按文档开发                |
| MySQL 性能瓶颈               | 低   | 中   | 索引优化 + 读写分离预留 + Redis 缓存热点数据             |
| WebSocket 连接管理（断线重连）| 中   | 中   | 心跳检测 + 自动重连 + 离线消息持久化                     |
| Flyway 迁移脚本冲突          | 中   | 高   | 严格的分支管理，迁移脚本合并前必须 rebase                |

---

## 十一、验收标准总表

### Phase 0 完成标准（启动期）
- [ ] `mvn clean compile` 后端编译成功
- [ ] `npm install && npm run dev` 前端启动成功
- [ ] `npm run lint && npm run typecheck` 前端检查通过
- [ ] `docker-compose up -d` 一键启动全部服务
- [ ] Flyway 自动创建 22 张表
- [ ] `develop` 分支已创建并推送
- [ ] 5 项遗留问题全部解决

### 项目整体完成标准
- [ ] 全部 9 大功能模块实现
- [ ] 三条核心业务线端到端跑通
- [ ] 核心模块测试覆盖率 ≥ 80%
- [ ] 核心接口响应时间 ≤ 500ms
- [ ] 安全测试通过（SQL 注入、XSS、CSRF）
- [ ] Docker Compose 可一键部署
- [ ] API 文档完整（Knife4j 自动生成）
- [ ] 用户文档完整（README + 部署文档）

---

**文档版本**：v1.0.0
**最后更新**：2026-06-28
