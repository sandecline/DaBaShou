# 管理后台模块 API契约

## 模块信息
- **模块**: dabashou-admin
- **前缀**: `/api/admin/v1`
- **认证**: 所有接口需要管理员角色

---

## 一、用户管理接口

### 1.1 用户列表
- **URL**: `GET /api/admin/v1/users`

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| keyword | String | 否 | 搜索关键词 |
| status | Integer | 否 | 状态筛选 |
| pageNum | Integer | 否 | 页码 |
| pageSize | Integer | 否 | 每页条数 |

- **响应**: `data = PageResult<UserAdminVo>`

### 1.2 用户详情
- **URL**: `GET /api/admin/v1/users/{id}`
- **响应**:
```json
{
  "id": 1,
  "username": "zhangsan",
  "nickname": "张三",
  "avatar": "url",
  "phone": "13800138000",
  "pointBalance": 100,
  "trustScore": 4.5,
  "campus": "主校区",
  "status": 1,
  "createTime": "2026-01-01 00:00:00",
  "lastLoginTime": "2026-07-01 10:00:00"
}
```
- **错误码**: 404-用户不存在

### 1.3 启用/禁用用户
- **URL**: `PUT /api/admin/v1/users/{id}/status`

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| status | Integer | 是 | 1-启用 0-禁用 |

- **响应**: `data = null`
- **错误码**: 404-用户不存在

### 1.4 重置密码
- **URL**: `POST /api/admin/v1/users/{id}/reset-password`
- **响应**: `data = { "newPassword": "abc123" }`
- **错误码**: 404-用户不存在

---

## 二、订单管理接口

### 2.1 订单列表
- **URL**: `GET /api/admin/v1/orders`

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| status | Integer | 否 | 状态筛选 |
| keyword | String | 否 | 搜索关键词 |
| pageNum | Integer | 否 | 页码 |
| pageSize | Integer | 否 | 每页条数 |

- **响应**: `data = PageResult<OrderAdminVo>`

### 2.2 订单详情
- **URL**: `GET /api/admin/v1/orders/{id}`
- **响应**: `data = OrderAdminVo`
- **错误码**: 404-订单不存在

### 2.3 订单仲裁
- **URL**: `POST /api/admin/v1/orders/{id}/arbitrate`

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| result | String | 是 | complete(完成) / refund(退款) |
| reason | String | 是 | 仲裁理由 |
| refundAmount | Integer | 否 | 退款积分数(退款时可指定) |

- **响应**: `data = null`
- **错误码**: 409-状态不允许

---

## 三、信用管理接口

### 3.1 违规处理
- **URL**: `POST /api/admin/v1/violations/{id}`

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| result | String | 是 | 处理结果 |

- **响应**: `data = null`
- **错误码**: 404-不存在

### 3.2 申诉审核
- **URL**: `POST /api/admin/v1/appeals/{id}`

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| approved | Boolean | 是 | 是否通过 |
| reason | String | 是 | 审核理由 |

- **响应**: `data = null`
- **错误码**: 404-不存在

---

## 四、校园认证审核

### 4.1 认证列表
- **URL**: `GET /api/admin/v1/campus-auths`

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| status | Integer | 否 | 状态筛选 |
| pageNum | Integer | 否 | 页码 |
| pageSize | Integer | 否 | 每页条数 |

- **响应**: `data = PageResult<CampusAuthVo>`

### 4.2 审核认证
- **URL**: `POST /api/admin/v1/campus-auths/{id}`

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| approved | Boolean | 是 | 是否通过 |
| reason | String | 否 | 拒绝理由 |

- **响应**: `data = null`
- **错误码**: 404-不存在, 409-已审核

---

**文档版本**: v1.3.0
**最后更新**: 2026-06-28
