# 搭把手（DaBaShou）后端完善实施方案

> 本文档为搭把手项目后端完善实施计划，基于对源码、数据库、文档的逐项核验编制，所有判断均有据可查。
>
> 技术栈：Spring Boot 3.4.3 + Java 21 + MyBatis-Plus + MySQL，Maven 多模块（13 个子模块），包名 `com.dabashou.{module}`。
>
> 编制日期：2026-06-29　　文档版本：v1.0.0

---

## 一、现状分析

### 1.1 模块完成度

| 模块 | 状态 | 依据 |
|---|---|---|
| dabashou-common | ✅ 已实现（11 类） | AjaxResult / BaseEntity / PageQuery / PageResult / ErrorCode / OrderStatus / PointTransType / TrustLevel / BusinessException / JwtUtil / SecurityUtil |
| dabashou-order | ⚠️ 骨架（17 类） | OrderController 14 接口 + OrderServiceImpl 状态机校验完整，但跨模块逻辑全 TODO |
| dabashou-api | ⚠️ 已实现但有问题 | 启动类 + 9 配置类 + DemoController（624 行，绕过架构直查数据库） |
| dabashou-user | ❌ 空壳 | 仅 package-info.java |
| dabashou-skill | ❌ 空壳 | 仅 package-info.java |
| dabashou-shelf | ❌ 空壳 | 仅 package-info.java |
| dabashou-demand | ❌ 空壳 | 仅 package-info.java |
| dabashou-point | ❌ 空壳 | 仅 package-info.java |
| dabashou-message | ❌ 空壳 | 仅 package-info.java |
| dabashou-credit | ❌ 空壳 | 仅 package-info.java |
| dabashou-stat | ❌ 空壳 | 仅 package-info.java |
| dabashou-admin | ❌ 空壳 | 仅 package-info.java |
| dabashou-system | ❌ 空壳 | 仅 package-info.java |

### 1.2 技术问题清单（按严重程度分级）

| 级别 | 问题 | 源码依据 |
|---|---|---|
| **P0** | DemoController 任意密码登录、硬编码 userId=1、无事务、confirm 直接 `UPDATE status=5` 无状态校验（违反订单状态机不可跳变约束） | DemoController L39 / L295 / L377 / L416 / L530 |
| **P0** | SecurityConfig 放行 `/api/user`、`/api/order` 等几乎所有业务路径，接口实际无鉴权 | SecurityConfig L41-49 |
| **P0** | Redis 配置矛盾：主 yml 排除 RedisAutoConfiguration，dev 又配 redis 连接 → Redis 不可用 | application.yml L12-14 / application-dev.yml L7-12 |
| **P0** | 缺 point 三表（account/freeze/guarantee_pool），积分余额冗余在 dbs_user.point_balance，无法支撑冻结/担保逻辑 | V1.0.0 仅 dbs_point_transaction |
| **P0** | 迁移脚本全部无回滚语句（违反 AGENTS.md 2.2 "迁移脚本必须可回滚"） | V1.0.0 / V1.2.0 均无 DROP/ALTER 回滚段 |
| **P0** | dbs_order.verify_code 无 UNIQUE 约束；Order 未继承 BaseEntity 且无 deleted 字段，与全局 logic-delete 配置冲突 | Order.java L10 / BaseEntity L25 / application.yml L34 |
| **P0** | 缺复合索引：order(buyer_id,status)/(seller_id,status)、demand(status,demand_type)、chat_message(session_id,create_time) | V1.0.0 L148-150 仅单列索引 |
| **P0** | 零种子数据：无管理员账号、无技能分类、无测试用户，新环境部署后无法登录 | grep INSERT 在 migration/*.sql 无结果 |
| **P1** | OrderServiceImpl 跨模块逻辑全 TODO：未从货架/需求取信息、支付未冻结积分、确认未结算、核销未校验码、争议/退款未退款 | OrderServiceImpl L33-49 / L114 / L217 / L248 / L269 |
| **P1** | 登录未用 BCrypt（PasswordEncoder Bean 已存在但未被调用） | DemoController L39 / SecurityConfig L29 |
| **P2** | 测试覆盖率 0%（AGENTS.md 5.2 要求核心模块 ≥80%） | 整个 backend 无任何测试类 |
| **P2** | 表前缀不统一（dbs_ / credit_ / user_ 三种混用，违反命名规范一致性） | V1.0.0 vs V1.2.0 |
| **P2** | DemoController 路径 `/api/` 无版本前缀，与 API 文档 `/api/v1/` 不一致 | order.md L5 vs DemoController |

### 1.3 数据库现状摘要

- 共 26 张表（核心业务 10 + 系统 8 + 信用 4 + 统计 2 + 聊天 2）
- 表名/字段名小写下划线：✅ 全部符合
- 中文注释：✅ 基本齐全
- 订单 status 注释：✅ 已标注 0-7 全部含义
- 信任分字段：✅ DECIMAL(3,1) 支持 0.0-5.0
- `database/migration` 与 `backend/src` 迁移脚本不同步（database 缺 V1.4.0）

### 1.4 文档现状摘要

- ✅ API 文档完整：`docs/api/` 下 11 模块 121 端点，自描述风格含 DTO/VO 定义
- ✅ 订单状态机文档完整：`docs/order-state-machine.md`
- ❌ 缺失：根 `README.md`、`docs/dev-plan.md`（本文件填补）、数据库设计文档（仅占位）、联调文档
- ⚠️ API 文档用 `/api/v1/` 前缀，DemoController 用 `/api/`，存在根本性不一致

---

## 二、完善目标

### 2.1 必须达成（6-8 周内）

1. DemoController 业务全部迁移到正式模块并删除（或仅留 health 接口）
2. 10 个空壳模块补齐 Controller / Service / Mapper / Entity / DTO / VO 完整实现
3. 数据库：补 point 三表、种子数据、回滚脚本、复合索引、verify_code 唯一约束、Order 继承 BaseEntity
4. 安全：SecurityConfig 强制鉴权、登录改 BCrypt、Redis 可用
5. 订单状态机闭环：积分冻结 / 结算 / 核销 / 争议 / 退款全链路打通，无状态跳变
6. 核心模块（order / point / user）单元测试覆盖率 ≥80%

### 2.2 争取达成

- 消息模块 WebSocket 实时联调
- 统计模块定时任务 + Redis 缓存
- 表前缀统一为 `dbs_`（消除 credit_ / user_ 前缀）
- 多环境配置（dev / prod 分离）

---

## 三、具体计划（按优先级）

### P0-1　数据库基线修复

**技术要点**：
- 新增迁移 `V1.4.0__point_tables.sql`：
  - `dbs_point_account`（user_id UK, available, frozen, total_earned, total_spent）
  - `dbs_point_freeze`（order_id UK, user_id, amount, status, freeze_time, release_time）
  - `dbs_guarantee_pool`（order_id UK, amount, status, settle_time）
- 新增 `V1.5.0__fix_indexes_and_constraints.sql`：
  - dbs_order 加复合索引 (buyer_id, status)、(seller_id, status)
  - dbs_order.verify_code 加 UNIQUE 约束
  - dbs_demand 加 (status, demand_type) 复合索引
  - dbs_chat_message 加 (session_id, create_time) 复合索引
  - dbs_order 加 deleted 字段（TINYINT DEFAULT 0，配合全局逻辑删除）
- 新增 `V1.6.0__seed_data.sql`：
  - 1 个管理员（admin / BCrypt 哈希）
  - 7 个技能分类（学业辅导、维修帮忙、设计美工、技术支持、运动陪练、音乐艺术、生活服务）
  - 若干技能标签
  - 3 个测试用户（zhangsan / lisi / wangwu，BCrypt 哈希）
- 所有新脚本末尾附回滚段（`-- ROLLBACK:` 注释 + DROP / ALTER 语句）
- 同步 `database/migration` 与 `backend/src` 两处迁移脚本目录

**验收标准**：
- [ ] 脚本可正向执行且可回滚执行
- [ ] Flyway 开启（spring.flyway.enabled=true）后验证通过
- [ ] verify_code 重复插入报 MySQL 1062 错误
- [ ] 种子数据导入后可用 admin / zhangsan 登录

---

### P0-2　安全与配置修复

**技术要点**：
- `SecurityConfig`：白名单仅保留 `/api/v1/auth/**`、`/api/v1/files/download/**`、`/ws/**`、`/swagger-ui/**`、`/v3/api-docs/**`；其余 `anyRequest().authenticated()`
- `application.yml`：移除 `RedisAutoConfiguration` 排除项，引入 `spring-boot-starter-data-redis`；OrderServiceImpl 用 RedisTemplate 存幂等 Token / 核销码（30min TTL）
- 登录改 BCrypt：UserServiceImpl.login 用 `passwordEncoder.matches(rawPassword, user.getPasswordHash())`
- 新增 `application-prod.yml`，敏感配置（JWT secret、DB 密码）走环境变量

**验收标准**：
- [ ] 未带 Token 访问 `/api/v1/order` 返回 401
- [ ] Redis 可连通（redis-cli ping 返回 PONG）
- [ ] 错误密码登录返回 400 + "用户名或密码错误"
- [ ] prod 环境配置不含明文密钥

---

### P0-3　订单状态机闭环

**技术要点**：
补全 OrderServiceImpl 所有 TODO，严格按状态机流转：

- `createOrderFromShelf / createOrderFromDemand`：跨模块通过 shelf / demand 模块 Service 接口（同进程 @Autowired，禁止跨库 join）获取 sellerId / title / pointAmount，校验不能自买自卖
- `payOrder`（1→2）：调用 `PointService.freeze(buyerId, amount, orderId)` 冻结买家积分，写 dbs_point_freeze；Redis 生成核销码（6 位，30min TTL）
- `startService`（2→3）：校验核销码已生成
- `verifyOrder`（3→4）：Redis 校验核销码匹配且未过期，更新订单 verify_code
- `confirmOrder`（4→5）：调用 `PointService.settle()` 解冻 → 转入卖家账户，写双方流水，更新担保池状态
- `cancelOrder`（0/1→0）：若已支付则调用 `PointService.unfreeze()` 退还买家
- `refundOrder`（2/3/4→6）：调用 `PointService.refund()` 解冻退还买家
- `arbitrateOrder`（→7）：按仲裁结果转为 COMPLETED 或 REFUNDED
- 所有写操作加 `@Transactional(rollbackFor = Exception.class)`

**验收标准**：
- [ ] 状态机全路径单元测试通过（含非法跳变拒绝）
- [ ] 积分余额 + 冻结额对账一致（账户表 = 流水表汇总）
- [ ] 无负余额场景
- [ ] 核销码过期后 confirm 失败

---

### P1-1　用户模块（dabashou-user）

**技术要点**：
- 内容：UserController（login / register / profile / update / campus-auth）、UserService、UserMapper、User 实体（继承 BaseEntity）、DTO（RegisterDto / LoginDto / UpdateProfileDto）、VO（UserVo）
- DB：dbs_user 已存在，补充 deleted 字段（配合逻辑删除）
- 逻辑：注册 BCrypt 加密 + 初始积分 100 + 信任分 5.0；登录返回 JWT；校园认证写 user_campus_auth
- 安全：密码脱敏（日志不打印明文）、参数校验（@Valid）、防重放（X-Idempotent-Token 头）

**验收标准**：
- [ ] 注册 → 登录 → 获取 profile 链路通
- [ ] 密码不以明文存储或返回（响应 VO 无 passwordHash 字段）
- [ ] 日志中密码字段显示为 ******

---

### P1-2　积分模块（dabashou-point）

**技术要点**：
- 内容：PointAccountController（balance / transactions）、PointService（freeze / unfreeze / settle / refund / reward）、三实体（PointAccount / PointFreeze / GuaranteePool）+ Mapper
- 逻辑：所有操作 `@Transactional`；冻结前校验 `available >= amount`；每步写 dbs_point_transaction 流水（含 balance_after 字段）
- 并发控制：冻结用 `UPDATE dbs_point_account SET available = available - ?, frozen = frozen + ? WHERE user_id = ? AND available >= ?`，影响行数 = 0 则抛 409

**验收标准**：
- [ ] 并发冻结不超额（10 并发线程冻结同一账户，总额不超余额）
- [ ] 流水表与账户余额对账一致
- [ ] 担保池状态随订单状态正确流转

---

### P1-3　技能货架 / 需求模块（dabashou-shelf, dabashou-demand, dabashou-skill）

**技术要点**：
- dabashou-skill：SkillCategoryController / SkillTagController（分类/标签 CRUD，管理员维护）
- dabashou-shelf：ShelfController（list / detail / publish / update / offshelf / my）
- dabashou-demand：DemandController（list / detail / publish / take / close / my）
- 逻辑：发布校验技能标签存在；上下架改 status；接单改 demand.status=2 并触发订单创建
- 安全：仅本人可编辑 / 下架自己的货架 / 需求（校验 user_id 匹配）
- 性能：分页查询用关联查询或批量注解消除 N+1，响应 ≤500ms

**验收标准**：
- [ ] 分页查询响应 ≤500ms
- [ ] 非本人调用更新 / 下架接口返回 403
- [ ] 下单 → 支付 → 核销 → 完成 e2e 链路通

---

### P2-1　信用评价模块（dabashou-credit）

**技术要点**：
- ReviewController（submit / list）、ViolationController（list / appeal）、AppealController（submit / approve / reject）
- 逻辑：评价仅订单 status=5 可提交，且 (order_id, reviewer_id) UK 防重；评价不可修改（不提供 update 接口）；违规写 credit_violation 并按规则扣信任分
- 信任分计算：违规扣分写 user_trust_score_log，触发 trust_score 更新

**验收标准**：
- [ ] 同一订单同一用户重复评价返回 409
- [ ] 评价提交后无修改接口可调用
- [ ] 违规记录触发信任分下降

---

### P2-2　消息模块（dabashou-message）

**技术要点**：
- 会话 / 消息 / 未读数接口；复用已有 WsAuthInterceptor + dbs_chat_session / dbs_chat_message 表
- WebSocket 鉴权：连接时校验 JWT，按 userId 建立会话
- 未读数：消息发送时 Redis INCR，已读时 DECR

**验收标准**：
- [ ] WebSocket 无 Token 连接被拒
- [ ] 未读数实时更新（≤1s 延迟）

---

### P2-3　统计模块（dabashou-stat）

**技术要点**：
- overview / user / daily / skill-heat / demand 接口
- `@Scheduled` 定时任务每日凌晨写 dbs_stat_daily
- 热点数据加 Redis 缓存（TTL 5min）

**验收标准**：
- [ ] 统计接口有数据返回
- [ ] 定时任务正常执行
- [ ] 缓存命中率可观测

---

### P3-1　DemoController 渐进式迁移

**规则**：
- 每补一个正式模块，从 DemoController 删除对应 `/api/xxx` 方法
- 路径统一从 `/api/xxx` 改为 `/api/v1/xxx`
- 过渡期路径双写：v1 新接口 + 旧 `/api/` 保留 1 周，文档标注 `@Deprecated`
- 最终 DemoController 删除或仅留 health 接口

---

### P3-2　测试补齐

**技术要点**：
- order / point / user 核心逻辑 JUnit5 + Mockito 单测
- OrderServiceImpl 状态机边界测试（所有合法流转 + 所有非法跳变）
- 积分并发测试（CountDownLatch 模拟并发冻结）
- 覆盖率工具：JaCoCo，目标 ≥80%

**验收标准**：
- [ ] core 模块覆盖率 ≥80%
- [ ] 状态机非法跳变全部被拒绝
- [ ] 并发场景无超额

---

## 四、API 变更同步要求

> **核心原则**：代码与文档必须保持一致，违反一致性阻断合并。

1. **同 PR 更新**：每次新增 / 修改 / 删除接口，必须在**同一个 PR 内**更新 `docs/api/` 对应 .md 文件（含 URL / 请求参数 / 响应结构 / 错误码 / 示例）

2. **路径迁移记录**：DemoController 接口迁移到正式模块时，路径 `/api/xxx` → `/api/v1/xxx`，须：
   - 更新对应 API 文档
   - 记录到 `docs/api/CHANGELOG.md`（变更日期、旧路径、新路径、影响范围）
   - 通知前端同学

3. **每周一致性检查**：每周五运行脚本比对 `@*Mapping` 注解路径与 `docs/api/*.md` 的 URL 一致性，不一致阻断合并

4. **版本管理**：
   - 新接口一律 v1
   - 破坏性变更走 v2，并标记旧版 `@Deprecated`，保留至少 1 个版本周期
   - 废弃接口在 API 文档顶部标注"已废弃"及替代接口

5. **文档评审**：API 文档变更需在 PR 描述中贴出 diff，由 reviewer 一并审查

---

## 五、实施步骤与时间安排

| 周 | 里程碑 | 涉及模块 | 关键任务 | 交付物 | 验收标准 |
|---|---|---|---|---|---|
| **W1** | 基线修复 | db / api | P0-1 数据库脚本 + 回滚 + 种子；P0-2 安全配置 + Redis + BCrypt | V1.4-V1.6 脚本；SecurityConfig 修复 | 脚本可回滚；无 Token 返 401；种子数据可登录 |
| **W2** | 用户 + 积分 | user / point | P1-1 用户模块；P1-2 积分模块（账户 / 冻结 / 流水） | user / point 全类 | 注册登录链路通；冻结超额拒绝 |
| **W3** | 订单闭环 | order | P0-3 补全 OrderServiceImpl 全部 TODO | 订单全链路 | 状态机单测通过；积分对账一致 |
| **W4** | 货架 + 需求 | shelf / demand / skill | P1-3 货架 / 需求发布查询上下架 | 3 模块全类 | 下单→支付→核销→完成 e2e 通 |
| **W5** | 信用 + 消息 | credit / message | P2-1 评价 / 违规 / 申诉；P2-2 会话 / 消息 | credit / message 全类 | 评价不可改；WebSocket 鉴权通 |
| **W6** | 统计 + 迁移 | stat / api | P2-3 统计；P3-1 删 DemoController 业务方法 | stat 全类；DemoController 清理 | DemoController 仅剩 health；统计接口有数据 |
| **W7** | 测试 + 文档 | 全部 | P3-2 核心单测覆盖率 ≥80%；补 dev-plan / 联调 / db 设计文档 | 测试报告 + 文档 | 覆盖率 ≥80%；API 文档与代码一致 |
| **W8** | 缓冲 + 联调 | 全部 | 端到端联调、性能优化、遗留修复 | 联调报告 | 接口 ≤500ms；无 P0 缺陷 |

---

## 六、风险与应对

| 风险 | 影响 | 应对措施 |
|---|---|---|
| Order 改继承 BaseEntity 影响存量数据 | 中 | 仅加 deleted 字段默认 0；不动已有字段类型（符合 AGENTS.md 2.1 "禁止修改字段类型"） |
| 迁移加索引锁表 | 高 | 在低峰执行；大表用 pt-osc 或 `ALGORITHM=INPLACE, LOCK=NONE` |
| DemoController 迁移期前端断线 | 高 | 路径双写过渡：v1 新接口 + 旧 `/api/` 保留 1 周，文档标注废弃 |
| 跨模块调用违反边界 | 中 | 同进程用模块 Service 接口（非实现类），禁止跨库 join；为后续抽 Feign 预留 |
| 积分并发超扣 | 高 | freeze 用 `UPDATE ... WHERE available >= ?` 乐观更新，影响行数 = 0 则抛 409 |
| Redis 依赖引入风险 | 中 | 先修配置矛盾使 Redis 可用；幂等 Token 可降级为 DB 唯一索引兜底 |
| 测试时间不足 | 中 | W3 起核心逻辑随开发补单测，W7 集中补齐，不积压到末尾 |
| 表前缀统一涉及改名 | 中 | 争取项，不强制；如做则用 `ALTER TABLE ... RENAME TO`，保留旧视图过渡 |

---

## 七、关键文件清单

### 待修改
- `backend/dabashou-api/src/main/java/com/dabashou/api/config/SecurityConfig.java` — 收紧白名单
- `backend/dabashou-api/src/main/resources/application.yml` / `application-dev.yml` — 修 Redis 矛盾，新增 prod
- `backend/dabashou-api/src/main/java/com/dabashou/api/controller/DemoController.java` — 渐进删除
- `backend/dabashou-order/src/main/java/com/dabashou/order/service/impl/OrderServiceImpl.java` — 补全 TODO
- `backend/dabashou-order/src/main/java/com/dabashou/order/domain/Order.java` — 继承 BaseEntity

### 待新增
- `database/migration/V1.4.0__point_tables.sql` — 积分三表
- `database/migration/V1.5.0__fix_indexes_and_constraints.sql` — 索引 + 约束
- `database/migration/V1.6.0__seed_data.sql` — 种子数据
- `backend/dabashou-user/src/main/java/com/dabashou/user/**` — 用户模块全套
- `backend/dabashou-point/src/main/java/com/dabashou/point/**` — 积分模块全套
- `backend/dabashou-shelf/src/main/java/com/dabashou/shelf/**` — 货架模块全套
- `backend/dabashou-demand/src/main/java/com/dabashou/demand/**` — 需求模块全套
- `backend/dabashou-skill/src/main/java/com/dabashou/skill/**` — 技能模块全套
- `backend/dabashou-credit/src/main/java/com/dabashou/credit/**` — 信用评价模块全套
- `backend/dabashou-message/src/main/java/com/dabashou/message/**` — 消息模块全套
- `backend/dabashou-stat/src/main/java/com/dabashou/stat/**` — 统计模块全套
- `docs/api/CHANGELOG.md` — API 变更记录

### 待同步更新
- `docs/api/*.md` — 随接口实现同步更新（路径 /api/ → /api/v1/）
- `docs/design/database-design.md` — 补全表结构 + ER 图（当前仅占位）
- `README.md`（项目根）— 补齐项目说明

---

**文档版本**：v1.0.0
**最后更新**：2026-06-29
**下次评审**：W3 结束时（订单闭环完成后）
