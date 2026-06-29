# 搭把手后端完整实施计划（执行版）

> 本文档基于已完成的 W1 基线修复和 W2 用户/积分模块，制定剩余 6 周的后端完成路线。所有任务可执行、可验收。
> 技术栈：Spring Boot 3.4.3 + Java 21 + MyBatis-Plus + MySQL + Redis。
> 文档版本：v1.0.0　　更新日期：2026-06-29

---

## 一、项目当前状态

### 1.1 后端模块完成度

| 模块 | 当前状态 | 说明 |
|---|---|---|
| **dabashou-common** | ✅ 完成 | 11 个类：AjaxResult、BaseEntity、PageResult、ErrorCode、OrderStatus、PointTransType、TrustLevel、BusinessException、JwtUtil、SecurityUtil、PageQuery |
| **dabashou-api** | ⚠️ 进行中 | 启动类 + 9 配置类 + DemoController（后续 W6 清理） |
| **dabashou-order** | ⚠️ 骨架完成 | 17 个类：Controller + Service 状态机校验完整，跨模块 TODO 待补 |
| **dabashou-user** | ✅ W2 完成 | 19 个 Java 文件：AuthController + UserController + Service + 4 VO + 7 DTO |
| **dabashou-point** | ✅ W2 完成 | 17 个 Java 文件：冻结/解冻/结算/退款/担保池 + PointController |
| **dabashou-skill** | ❌ 空壳 | 仅 package-info.java |
| **dabashou-shelf** | ❌ 空壳 | 仅 package-info.java |
| **dabashou-demand** | ❌ 空壳 | 仅 package-info.java |
| **dabashou-message** | ❌ 空壳 | 仅 package-info.java |
| **dabashou-credit** | ❌ 空壳 | 仅 package-info.java |
| **dabashou-stat** | ❌ 空壳 | 仅 package-info.java |
| **dabashou-admin** | ❌ 空壳 | 仅 package-info.java |
| **dabashou-system** | ❌ 空壳 | 仅 package-info.java |

**进度：2/8 周，核心公共模块 + 用户 + 积分已落地，约 30% 完成度。**

### 1.2 已解决的关键隐患

- 数据库已补齐积分三表（dbs_point_account / dbs_point_freeze / dbs_guarantee_pool）
- 已加复合索引、verify_code UNIQUE 约束、deleted 字段
- 已导入种子数据（admin + 7 分类 + 35 标签 + 3 测试用户 + 货架 + 需求）
- SecurityConfig 已收紧白名单，未鉴权访问返回 401 JSON
- Redis 配置矛盾已修复
- Order 已继承 BaseEntity，支持逻辑删除
- 登录已改 BCrypt，注册同步创建积分账户
- 积分冻结已用乐观更新防超扣

---

## 二、剩余后端完成目标

### 2.1 必须达成（W3-W8）

1. **订单状态机完全闭环**：创建 → 支付（冻结）→ 开始服务 → 核销 → 确认（结算）→ 完成
2. **技能/货架/需求三大模块**：分类标签管理、货架发布上下架、需求发布接单
3. **信用/消息/统计模块**：评价、违规、申诉、会话、消息、实时统计
4. **DemoController 迁移并删除**：业务方法全部迁移到正式模块，仅保留 health 接口
5. **核心模块测试覆盖率 ≥80%**：order、point、user 三模块
6. **文档补齐**：根 README、数据库设计文档、联调文档、API CHANGELOG
7. **API 文档与代码完全一致**：所有接口路径统一为 `/api/v1/`

### 2.2 争取达成

- WebSocket 实时消息端到端联调
- 统计模块定时任务 + Redis 缓存
- 表前缀统一（credit_ / user_ → dbs_）
- application-prod.yml 生产环境配置

---

## 三、详细执行计划（W3-W8）

## W3：订单状态机闭环（P0）

**目标**：补全 `OrderServiceImpl` 全部 TODO，实现积分冻结、核销码、结算、退款全链路。

### 3.1 任务清单

| 任务 | 技术要点 | 验收标准 |
|---|---|---|
| W3-1 创建订单补全 | 从 shelf/demand 模块读取 sellerId/title/pointAmount；校验不能自买自卖；生成订单号 | 创建订单后 buyer/seller/shelfId/title/pointAmount 全部正确 |
| W3-2 支付订单 | 调用 `PointService.freeze(buyerId, amount, orderId)`；生成 6 位核销码存 Redis（30min TTL）；记录幂等 Token | 支付后积分冻结成功，Redis 可查到核销码，重复支付返回 409 |
| W3-3 取消订单 | 已支付订单调用 `PointService.unfreeze(orderId)`；记录取消原因和时间 | 待支付/已支付订单均可取消，已支付取消后积分返还 |
| W3-4 开始服务 | 校验卖家身份；状态机 2→3 | 已支付订单可开始服务，非卖家返回 403 |
| W3-5 核销订单 | Redis 校验核销码匹配且未过期；状态机 3→4 | 正确核销码通过，错误/过期核销码拒绝 |
| W3-6 确认完成 | 调用 `PointService.settle(orderId)`；状态机 4→5 | 卖家账户正确增加，担保池状态更新 |
| W3-7 争议/仲裁 | 状态机 4→7；仲裁结果决定 7→5 或 7→6；退款调用 `PointService.refund` | 争议状态可仲裁，退款正确返还买家 |
| W3-8 订单列表/详情 | 关联用户表查昵称头像；按 buyer/seller 筛选；分页 ≤500ms | 列表返回完整字段，分页响应 ≤500ms |
| W3-9 单测补全 | 状态机全路径合法/非法测试；积分对账测试；核销码过期测试 | 覆盖 80%+

### 3.2 跨模块依赖

- 依赖 `PointService`（W2 已完成）
- 需要 `ShelfService` / `DemandService` 接口：在 W3 先用本地 JdbcTemplate 或 stub 实现，W4 替换为正式模块 Service

### 3.3 交付物

- 修改 `OrderServiceImpl.java` 全部 TODO 完成
- 新增订单相关单元测试
- 订单 API 文档确认无需修改（已是 `/api/v1/order`）

---

## W4：货架 + 需求 + 技能模块（P1）

**目标**：补齐 dabashou-skill、shelf、demand 三个空壳模块，使订单可以真正从货架/需求创建。

### 4.1 技能模块（dabashou-skill）

| 任务 | 接口/内容 | 验收标准 |
|---|---|---|
| S1-1 分类管理 | GET /api/v1/categories（列表）；GET /api/v1/categories/{id}；POST /api/v1/admin/categories（管理员新增） | 分类 CRUD 可运行，种子数据 7 条可读取 |
| S1-2 标签管理 | GET /api/v1/tags；POST /api/v1/admin/tags；DELETE /api/v1/admin/tags/{id} | 标签与分类关联，重复名返回 409 |
| S1-3 用户技能 | GET /api/v1/user-skills；POST /api/v1/user-skills；DELETE /api/v1/user-skills/{id} | 用户可维护自己的技能标签 |

### 4.2 货架模块（dabashou-shelf）

| 任务 | 接口/内容 | 验收标准 |
|---|---|---|
| S2-1 货架发布 | POST /api/v1/shelf；参数：title, description, categoryId, tagIds, pointAmount, timeSlots | 发布成功 status=1 待审核，非本人不可编辑 |
| S2-2 货架列表 | GET /api/v1/shelf（分页+筛选：categoryId, keyword, sort）；GET /api/v1/shelf/{id} | 分页 ≤500ms，详情返回 timeSlots |
| S2-3 我的货架 | GET /api/v1/shelf/my；PUT /api/v1/shelf/{id}；PUT /api/v1/shelf/{id}/off | 仅本人可修改/下架 |
| S2-4 订单创建依赖 | 提供 `ShelfService.getShelfInfo(Long id)` 供 Order 模块调用 | 跨模块接口返回 sellerId/title/pointAmount |

### 4.3 需求模块（dabashou-demand）

| 任务 | 接口/内容 | 验收标准 |
|---|---|---|
| S3-1 需求发布 | POST /api/v1/demand；参数：title, categoryId, tagIds, pointAmount, deadline, location | 发布成功 status=0 待接单 |
| S3-2 需求列表 | GET /api/v1/demand；GET /api/v1/demand/{id} | 分页 ≤500ms |
| S3-3 接单/关闭 | POST /api/v1/demand/{id}/take；POST /api/v1/demand/{id}/close | 接单后 status=2 进行中并触发订单创建 |
| S3-4 我的需求 | GET /api/v1/demand/my；PUT /api/v1/demand/{id} | 仅本人可修改/关闭 |
| S3-5 订单创建依赖 | 提供 `DemandService.getDemandInfo(Long id)` 供 Order 模块调用 | 跨模块接口返回 buyerId/title/pointAmount |

### 4.4 交付物

- 3 个模块全套 Java 文件（约 50+ 文件）
- 订单模块替换 W3 中的 stub，调用正式 ShelfService/DemandService
- 货架/需求/分类 API 文档确认
- e2e 链路：货架发布 → 下单 → 支付 → 核销 → 完成

---

## W5：信用评价 + 消息模块（P2）

### 5.1 信用评价模块（dabashou-credit）

| 任务 | 接口/内容 | 验收标准 |
|---|---|---|
| C1-1 评价提交 | POST /api/v1/credit/reviews；仅 order status=5 可评；(order_id, reviewer_id) UK 防重 | 重复评价返回 409 |
| C1-2 评价列表 | GET /api/v1/credit/reviews/{userId}；GET /api/v1/credit/reviews/me | 返回评价列表、平均分 |
| C1-3 违规记录 | POST /api/v1/credit/violations（举报）；GET /api/v1/credit/violations | 违规记录触发 trust_score 扣分并写 user_trust_score_log |
| C1-4 申诉处理 | POST /api/v1/credit/appeals；GET /api/v1/credit/appeals/{id}；管理员审批 | 申诉状态流转：待审核 → 通过/拒绝 |
| C1-5 信任分计算 | 评价星级加权 + 违规扣分；计算后更新 dbs_user.trust_score | 评分变化有日志记录 |

### 5.2 消息模块（dabashou-message）

| 任务 | 接口/内容 | 验收标准 |
|---|---|---|
| M1-1 会话接口 | GET /api/v1/message/sessions；POST /api/v1/message/sessions；GET /api/v1/message/sessions/{id}/messages | 会话列表分页正常 |
| M1-2 消息接口 | POST /api/v1/message/send；GET /api/v1/message/unread-count；PUT /api/v1/message/read/{sessionId} | 发送消息成功，未读数正确 |
| M1-3 WebSocket | 复用 WsAuthInterceptor；连接时校验 JWT；按 userId 路由消息 | 无 Token 连接被拒；消息实时送达 |
| M1-4 未读数缓存 | Redis INCR/DECR 未读数；已读时清 0 | 未读数实时更新 |

### 5.3 交付物

- credit / message 模块全套代码
- 评价提交后触发信任分更新
- WebSocket 通过 Postman/前端联调

---

## W6：统计模块 + DemoController 清理（P2/P3）

### 6.1 统计模块（dabashou-stat）

| 任务 | 接口/内容 | 验收标准 |
|---|---|---|
| T1-1 数据看板 | GET /api/v1/stat/overview | 返回总订单数、总交易额、用户数、活跃货架数 |
| T1-2 用户统计 | GET /api/v1/stat/user | 返回个人订单/积分/评价统计 |
| T1-3 每日统计 | GET /api/v1/stat/daily | 返回近 7 天订单/注册/活跃趋势 |
| T1-4 技能热度 | GET /api/v1/stat/skill-heat | 返回分类热度排行 |
| T1-5 定时任务 | @Scheduled 每日凌晨 2 点写 dbs_stat_daily | 定时任务可执行，表有数据 |
| T1-6 缓存 | Redis 缓存热点统计（TTL 5min） | 缓存命中率可观测 |

### 6.2 DemoController 清理

| 任务 | 内容 | 验收标准 |
|---|---|---|
| D1-1 方法迁移 | 将 DemoController 中所有业务方法迁移到正式模块 | 每个方法都有对应 v1 接口 |
| D1-2 路径双写结束 | 删除旧 `/api/xxx` 路径，仅保留 `/api/v1/xxx` | 旧路径返回 404 |
| D1-3 健康接口保留 | DemoController 仅保留 `/health` 或 `/api/health` | 健康检查可用 |
| D1-4 文档更新 | 更新 docs/api/CHANGELOG.md | 记录所有迁移路径 |

### 6.3 交付物

- stat 模块全套代码
- DemoController 清理完毕，仅保留健康检查
- API CHANGELOG 更新

---

## W7：测试 + 文档（P3）

### 7.1 测试补齐

| 任务 | 内容 | 验收标准 |
|---|---|---|
| TEST-1 用户模块测试 | UserServiceImpl 注册/登录/改密/校园认证单测 | 覆盖率 ≥80% |
| TEST-2 积分模块测试 | 冻结/解冻/结算并发测试 | 覆盖率 ≥80% |
| TEST-3 订单模块测试 | 状态机全路径 + 非法跳变 + 积分对账 | 覆盖率 ≥80% |
| TEST-4 货架/需求测试 | 发布/查询/上下架/接单测试 | 核心逻辑冒烟通过 |
| TEST-5 集成测试 | 下单 → 支付 → 核销 → 完成 e2e | 关键路径通过 |
| TEST-6 JaCoCo 报告 | 生成覆盖率报告 | 看板展示核心模块覆盖率 |

### 7.2 文档补齐

| 任务 | 内容 | 验收标准 |
|---|---|---|
| DOC-1 根 README.md | 项目介绍、技术栈、快速启动、模块说明 | 内容完整 |
| DOC-2 数据库设计文档 | docs/design/database-design.md 表结构 + ER 图 + 索引说明 | 补齐占位 |
| DOC-3 联调文档 | docs/dev-integration.md 前后端接口对照、WebSocket 地址、测试账号 | 前端可据此联调 |
| DOC-4 API CHANGELOG | docs/api/CHANGELOG.md 记录所有路径变更 | 与代码一致 |
| DOC-5 一致性检查 | 脚本比对代码 @*Mapping 与 docs/api/*.md | 无差异 |

### 7.3 交付物

- 测试报告（覆盖率 ≥80%）
- 4 份文档补齐
- API 文档与代码一致性检查通过

---

## W8：联调 + 性能优化 + 缓冲（收尾）

### 8.1 任务清单

| 任务 | 内容 | 验收标准 |
|---|---|---|
| INT-1 端到端联调 | 前端全流程：注册 → 登录 → 发布货架 → 下单 → 支付 → 聊天 → 核销 → 评价 | 全流程无阻断 |
| INT-2 接口性能 | 核心接口压测，响应 ≤500ms | 列表/详情接口 P95 ≤500ms |
| INT-3 安全审计 | 检查密码是否加密、SQL 是否参数化、敏感日志是否脱敏 | 无 P0/P1 安全问题 |
| INT-4 数据库审计 | 检查所有索引命中、慢查询日志 | 无全表扫描 |
| INT-5 遗留修复 | 处理 W3-W7 的 bug 和未完成任务 | 问题清单清空 |
| INT-6 生产配置 | application-prod.yml + Docker 部署说明 | 可部署到测试环境 |

### 8.2 交付物

- 联调报告
- 性能测试报告
- 最终后端发布版本

---

## 四、各模块文件清单预估

| 模块 | 预计新增文件数 | 关键文件 |
|---|---|---|
| dabashou-order | 5-8 | OrderServiceImpl 修改、单测 |
| dabashou-skill | 15-20 | CategoryController、TagController、UserSkillController |
| dabashou-shelf | 15-20 | ShelfController、ShelfService、ShelfMapper |
| dabashou-demand | 15-20 | DemandController、DemandService、DemandMapper |
| dabashou-credit | 15-20 | ReviewController、ViolationController、AppealController |
| dabashou-message | 15-20 | ChatSessionController、ChatMessageController、WebSocketHandler |
| dabashou-stat | 10-15 | StatController、StatJob、StatMapper |
| dabashou-admin | 5-10 | 用户管理、订单管理、审核管理 |
| 测试 + 文档 | 20-30 | JUnit 测试类、README、DB 设计、联调文档 |
| **合计** | **120-160** | — |

---

## 五、关键依赖关系

```
W3 订单闭环
  ↓ 依赖 W2 PointService（已完成）
  ↓ 依赖 W4 ShelfService/DemandService（W3 可用 stub，W4 替换）

W4 货架/需求
  ↓ 依赖 W2 用户认证（已完成）
  ↓ 依赖 W3 订单创建回调（需同步定义接口）

W5 信用/消息
  ↓ 依赖 W4 订单完成（评价需要已完成订单）
  ↓ 依赖 W2 用户模块（信任分更新）

W6 统计/Demo 清理
  ↓ 依赖 W3-W5 业务数据
  ↓ 依赖 W4 货架/需求数据

W7 测试/文档
  ↓ 依赖 W3-W6 全部完成

W8 联调/收尾
  ↓ 依赖 W7 全部完成
```

---

## 六、风险与应对

| 风险 | 影响 | 应对 |
|---|---|---|
| 订单模块跨模块依赖复杂 | 高 | W3 先 stub 调用，W4 替换；定义稳定的 Service 接口契约 |
| 并发积分冻结超扣 | 高 | 已用乐观更新 `WHERE available >= ?`，并加单测验证 |
| DemoController 清理导致前端断线 | 高 | 过渡期保留双写 1 周；提前通知前端 |
| 测试时间不足 | 中 | 核心模块随开发同步写单测，不积压到 W7 |
| WebSocket 实时性不达标 | 中 | 先做 HTTP 轮询兜底方案，再优化 WebSocket |
| 表前缀不统一 | 低 | 作为 W8 争取项，不阻塞主线 |

---

## 七、验收总表

| 验收项 | 验收标准 | 负责阶段 |
|---|---|---|
| 订单状态机 | 8 个状态全路径合法；非法跳变全部拒绝 | W3 |
| 积分系统 | 并发冻结不超额；流水与账户余额对账一致 | W2/W3 |
| 用户认证 | 注册/登录/JWT/改密/校园认证链路通 | W2 |
| 货架/需求 | 发布/查询/下单/接单 e2e 通 | W4 |
| 信用评价 | 评价不可改；违规触发扣分 | W5 |
| 消息 | WebSocket 鉴权通过；未读数实时 | W5 |
| Demo 清理 | 旧 `/api/xxx` 全部下线，文档已更新 | W6 |
| 测试覆盖率 | order/point/user ≥80% | W7 |
| 文档一致性 | API 代码与 docs/api/*.md 一致 | W7 |
| 性能 | 核心接口 P95 ≤500ms | W8 |

---

**计划制定**：WorkBuddy
**版本**：v1.0.0
**下次评审**：W3 结束时（订单闭环完成后）
