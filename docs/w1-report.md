# W1 基线修复实施报告

## 完成摘要

按要求执行 dev-plan.md 的 W1（基线修复），涵盖 P0-1 数据库基线修复 + P0-2 安全与配置修复，共 4 个任务全部完成。

## 关键变更

### 数据库（3 个新迁移脚本 + 1 个同步）

| 脚本 | 内容 | 状态 |
|---|---|---|
| V1.5.0 | dbs_point_account / dbs_point_freeze / dbs_guarantee_pool 三表 | ✅ |
| V1.6.0 | 复合索引(constraints) + verify_code UNIQUE + deleted 字段 | ✅ |
| V1.7.0 | 种子数据：1 admin + 7 分类 + 35 标签 + 3 用户 + 5 货架 + 4 需求 | ✅ |
| V1.4.0 | （同步）从 backend/src 复制到 database/migration/ | ✅ |

所有脚本含回滚段，database/migration/ 与 backend/src 两处现已同步。

### 安全配置

- SecurityConfig 白名单从 16 条放行收紧为 3 组：公开接口 + Demo 过渡 + admin 鉴权
- 添加 AuthenticationEntryPoint（未认证返回 401 JSON）和 AccessDeniedHandler（权限不足返回 403 JSON）
- application.yml 移除 Redis 自动配置排除项

### 实体修复

- Order.java 继承 BaseEntity → 获得 id / createTime / updateTime / @TableLogic deleted

## 下一步（W2 用户+积分）

按 dev-plan 计划，W2 需实施：
- P1-1 用户模块 dabashou-user（Controller/Service/Mapper/Entity/DTO/VO）
- P1-2 积分模块 dabashou-point（账户/冻结/流水/担保池）

## 注意

- Flyway 仍为 `enabled: false`，需在 W3 订单闭环完成后手动开启验证
- Demo 过渡接口 `/api/**` 仍 permitAll（计划 W6 下线）
