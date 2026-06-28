# 搭把手 全局API契约规范

## 一、统一响应格式

所有接口返回统一的JSON格式：

```json
{
  "code": 200,
  "msg": "success",
  "data": {}
}
```

| 字段 | 类型 | 说明 |
|------|------|------|
| code | int | 状态码，200表示成功 |
| msg | String | 提示信息 |
| data | Object | 业务数据，可为null |

## 二、错误码表

| 错误码 | 含义 | 说明 |
|--------|------|------|
| 200 | 操作成功 | 正常返回 |
| 400 | 请求参数错误 | 参数校验失败 |
| 401 | 未授权 | 未登录或token过期 |
| 403 | 禁止访问 | 无权限 |
| 404 | 资源不存在 | 数据不存在 |
| 409 | 状态冲突 | 状态机不允许的操作 |
| 429 | 请求过于频繁 | 触发限流 |
| 500 | 服务器内部错误 | 系统异常 |

### 错误码与HTTP状态码映射

> **说明**: 业务错误码位于响应体`code`字段，HTTP状态码位于HTTP响应头。正常情况下HTTP状态码始终为200，业务错误码在body中体现。

| 业务错误码 | HTTP状态码 | 场景 |
|-----------|-----------|------|
| 200 | 200 | 操作成功 |
| 400 | 200 | 参数校验失败、请求体解析失败 |
| 401 | 200 | 未登录、token过期、密码错误 |
| 403 | 200 | 无权限访问 |
| 404 | 200 | 资源不存在 |
| 409 | 200 | 状态机冲突、业务规则冲突 |
| 429 | 200 | 限流触发 |
| 500 | 500 | 系统异常（仅系统异常时HTTP状态码非200） |

## 三、分页规范

### 请求参数
| 参数 | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| pageNum | int | 否 | 1 | 页码，从1开始 |
| pageSize | int | 否 | 10 | 每页条数，最大100 |

### 响应格式
```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "total": 100,
    "list": [],
    "pageNum": 1,
    "pageSize": 10
  }
}
```

## 四、认证机制

### JWT Bearer Token
- **请求头**: `Authorization: Bearer <token>`
- **Token有效期**: 24小时（可配置）
- **刷新Token有效期**: 7天

### 公开接口（无需认证）
- `POST /api/v1/auth/register` - 用户注册
- `POST /api/v1/auth/login` - 密码登录
- `POST /api/v1/auth/sms-code` - 发送验证码
- `POST /api/v1/auth/sms-login` - 短信登录
- `GET /api/v1/files/download/**` - 文件下载
- `/doc.html` - Knife4j文档
- `/v3/api-docs/**` - OpenAPI文档

### 管理员接口
- 所有 `/api/admin/v1/**` 接口需要管理员角色

## 五、幂等Token

以下关键接口需要幂等Token防止重复提交：

| 接口 | 请求头 |
|------|--------|
| 创建订单 | X-Idempotent-Token |
| 支付订单 | X-Idempotent-Token |
| 核销订单 | X-Idempotent-Token |
| 仲裁订单 | X-Idempotent-Token |

- Token格式: UUID字符串
- Token有效期: 5分钟
- 同一Token只能使用一次

## 六、接口版本

- **用户端前缀**: `/api/v1/`
- **管理端前缀**: `/api/admin/v1/`
- **版本策略**: 向后兼容，废弃接口标记`@Deprecated`

## 七、日期时间格式

- **请求参数**: `yyyy-MM-dd HH:mm:ss`
- **响应数据**: `yyyy-MM-dd HH:mm:ss`
- **日期字段**: `yyyy-MM-dd`

## 八、Knife4j文档入口

- **文档地址**: `http://localhost:8080/doc.html`
- **OpenAPI JSON**: `http://localhost:8080/v3/api-docs`

## 九、请求规范

### Content-Type
- 查询接口: `application/json`
- 文件上传: `multipart/form-data`

### 参数校验
- 使用Jakarta Validation注解
- `@NotBlank` - 非空字符串
- `@NotNull` - 非空对象
- `@Size` - 长度限制
- `@Min` / `@Max` - 数值范围
- `@Pattern` - 正则校验

## 十、跨域配置

- **允许源**: 所有（开发环境）
- **允许方法**: GET, POST, PUT, DELETE, OPTIONS
- **允许头**: 所有
- **凭证**: 支持
- **缓存**: 3600秒

## 十一、命名规范

### URL命名
- 使用小写字母和连字符: `/api/v1/skill-tags`
- 资源使用复数名词: `/api/v1/shelves`, `/api/v1/demands`, `/api/v1/points`
- 子资源使用嵌套: `/api/v1/orders/{id}/verify`
- 操作使用动词: `/api/v1/orders/{id}/pay`, `/api/v1/orders/{id}/cancel`
- **存量例外**: order模块使用单数`/api/v1/order`（已上线，不做回改）

### 字段命名
- 使用小驼峰: `createTime`, `userId`, `orderNo`
- 布尔字段使用is前缀: `isRead`, `isAnonymous`
- 时间字段使用Time后缀: `createTime`, `updateTime`, `deleteTime`
- ID字段使用Id后缀: `userId`, `orderId`, `shelfId`

### 状态码命名
- 状态值使用整数: `status: 1`
- 状态描述使用中文: `statusDesc: "待支付"`
- 类型值使用整数: `type: 1`
- 类型描述使用中文: `typeDesc: "收入"`

## 十二、日志与追踪

### TraceId
- 每个请求自动生成唯一traceId
- 通过MDC注入，日志自动输出
- 响应头返回: `X-Trace-Id: {uuid}`

### 慢请求告警
- 阈值: 3秒
- 日志级别: WARN
- 包含: method, uri, duration, status

---

**文档版本**: v1.3.0
**最后更新**: 2026-06-28
