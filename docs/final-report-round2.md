# 搭把手二轮整改报告

**日期**：2026年7月1日  
**依据**：`docs/前后端接口与数据库逻辑整改二轮计划书.md`

## 一、已完成整改

### 1. 后台管理接口闭环

新增 `dabashou-admin` 后台接口，统一前缀为 `/api/admin/v1`，并在 Controller 层使用 `@PreAuthorize("hasRole('ADMIN')")`：

| 模块 | 接口 |
|------|------|
| 用户管理 | `GET /users`、`GET /users/{id}`、`PUT /users/{id}/status`、`POST /users/{id}/reset-password` |
| 订单管理 | `GET /orders`、`GET /orders/{id}`、`POST /orders/{id}/arbitrate` |
| 信用管理 | `GET /violations`、`POST /violations/{id}`、`GET /appeals`、`POST /appeals/{id}`、`GET /reviews`、`DELETE /reviews/{id}` |
| 校园认证 | `GET /campus-auths`、`POST /campus-auths/{id}` |
| 系统配置 | `GET /config`、`PUT /config` |

### 2. 权限与登录安全

- JWT `roles` claim 改为从 `sys_role`、`sys_user_role` 加载。
- 管理员账号通过迁移脚本绑定 `ADMIN` 角色。
- 普通用户默认补充 `USER` 角色。
- 禁用账号登录与刷新 token 均返回业务错误，不再签发新 token。
- 新增 `dbs_user.token_version`，访问 token 和刷新 token 均携带版本号；禁用/启用用户或重置密码后版本递增，旧 token 立即失效。
- 由于当前 `application.yml` 中 Flyway 默认关闭，代码对尚未执行 `V1.16.0` 的旧开发库做了兼容：未发现 `token_version` 字段时仍允许正常登录；执行迁移后自动启用旧 token 即时失效能力。
- `/api/admin/**` 后端仍由 Spring Security 统一拦截，普通用户访问返回 HTTP 403。

### 3. 审计与业务规则

- 后台用户状态修改、重置密码、订单仲裁、违规处理、申诉处理、认证审核、配置更新均写入 `sys_log`。
- 订单仲裁复用 `OrderService.arbitrateOrder`，不绕过订单状态机。
- 评价删除改为软隐藏：新增 `dbs_review.hidden`，后台删除接口更新为 `hidden=1`。
- 系统配置更新使用白名单，拒绝任意 key 和敏感 key 写入。

### 4. 数据库迁移与索引

新增迁移脚本：

- MySQL：`backend/dabashou-api/src/main/resources/db/migration/V1.16.0__admin_indexes_and_constraints.sql`
- H2：`backend/dabashou-api/src/test/resources/db/migration-h2/V1.16.0__admin_indexes_and_constraints.sql`

补齐内容：

- `dbs_skill_shelf(user_id, status)`
- `dbs_skill_shelf(skill_tag_id, status)`
- `dbs_demand(user_id, status)`
- `dbs_demand(skill_tag_id, status)`
- `dbs_chat_message(session_id, create_time)`
- `dbs_notification(user_id, is_read, create_time)`
- `dbs_review.hidden`
- `dbs_user.token_version`
- `sys_role`、`sys_user_role` 初始化与后台配置白名单数据

手机号唯一索引未直接添加，迁移脚本中保留前置核查 SQL：

```sql
SELECT phone, COUNT(*) FROM dbs_user WHERE phone IS NOT NULL AND phone <> '' GROUP BY phone HAVING COUNT(*) > 1;
SELECT COUNT(*) FROM dbs_user WHERE phone IS NULL OR phone = '';
```

### 5. 文档与前端契约

- 更新 `docs/api/admin.md`，补齐后台列表、评价、配置接口。
- 更新 `docs/api/README.md`，统一后台前缀为 `/api/admin/v1`。
- 更新 `frontend/src/api/README.md`，说明前端 `/admin/v1/**` 到后端 `/api/admin/v1/**` 的路径映射。
- `frontend/src/api/admin.ts` 已与新增后端 Controller 对齐，无需删除调用。

## 二、验证结果

### 后端

执行命令：

```powershell
mvn -gs C:\Users\Li\tmp\codex-maven-settings.xml -s C:\Users\Li\tmp\codex-maven-settings.xml test
```

结果：

- `BUILD SUCCESS`
- `Tests run: 24, Failures: 0, Errors: 0, Skipped: 0`
- H2 Flyway 迁移成功到 `v1.16.0`

说明：本机 Maven 安装目录 `D:\edge\apache-maven-4.0.0-rc-5-bin\apache-maven-4.0.0-rc-5\conf\settings.xml` 存在 XML 解析错误，直接执行 `mvn test` 会失败。本次使用临时空 settings 绕过该环境问题。

### 前端

执行命令：

```powershell
npm run build
```

结果：构建通过。仅保留现有 Sass legacy API、Rollup 注释和 chunk size 警告。

## 三、剩余风险

| 风险 | 说明 | 后续建议 |
|------|------|----------|
| 手机号唯一索引未落库 | 需要先确认生产数据重复和空值情况 | 数据清洗后单独新增唯一索引迁移 |
| 后台聚合查询使用 JdbcTemplate | 二轮按计划采用最小闭环，仍存在跨模块查询 | 三轮抽模块 Facade/API |
| 角色权限粒度较粗 | 当前只做 `ADMIN` 总角色 | 后续补权限码和菜单按钮级鉴权 |
| 系统管理文档仍有角色/权限扩展接口 | 本轮只补计划书要求的后台配置接口 | 三轮补完整 RBAC 管理接口或标记未实现 |
