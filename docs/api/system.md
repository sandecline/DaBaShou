# 系统管理模块 API契约

## 模块信息
- **模块**: dabashou-system
- **前缀**: `/api/v1/files`, `/api/admin/v1`

---

## 一、文件接口

### 1.1 文件上传
- **URL**: `POST /api/v1/files/upload`
- **Content-Type**: `multipart/form-data`

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| file | File | 是 | 文件内容 |

- **响应**:
```json
{
  "id": 1,
  "fileName": "abc123.jpg",
  "fileUrl": "/files/abc123.jpg",
  "fileSize": 1024,
  "fileType": "image/jpeg"
}
```
- **限制**: 单文件最大10MB
- **错误码**: 400-文件为空, 400-文件过大

### 1.2 文件下载
- **URL**: `GET /api/v1/files/{id}/download`
- **响应**: 文件流
- **错误码**: 404-文件不存在

---

## 二、角色接口（管理员）

### 2.1 角色列表
- **URL**: `GET /api/admin/v1/roles`
- **响应**:
```json
[
  {
    "id": 1,
    "roleCode": "ADMIN",
    "roleName": "管理员",
    "description": "系统管理员",
    "status": 1,
    "createTime": "2026-01-01 00:00:00"
  }
]
```

### 2.2 创建角色
- **URL**: `POST /api/admin/v1/roles`

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| roleCode | String | 是 | 角色编码 |
| roleName | String | 是 | 角色名称 |
| description | String | 否 | 描述 |

- **响应**: `data = { "id": 1 }`
- **错误码**: 409-编码已存在

### 2.3 更新角色
- **URL**: `PUT /api/admin/v1/roles/{id}`
- **请求体**: 同2.2（所有字段可选）
- **响应**: `data = null`
- **错误码**: 404-不存在

### 2.4 删除角色
- **URL**: `DELETE /api/admin/v1/roles/{id}`
- **响应**: `data = null`
- **错误码**: 404-不存在, 409-角色已分配

---

## 三、权限接口（管理员）

### 3.1 权限树
- **URL**: `GET /api/admin/v1/permissions/tree`
- **响应**:
```json
[
  {
    "id": 1,
    "parentId": 0,
    "permissionCode": "user:view",
    "permissionName": "查看用户",
    "type": 1,
    "path": "/admin/users",
    "component": "UserList",
    "icon": "user",
    "sortOrder": 1,
    "children": []
  }
]
```

### 3.2 分配角色权限
- **URL**: `POST /api/admin/v1/roles/{roleId}/permissions`

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| permissionIds | Long[] | 是 | 权限ID列表 |

- **响应**: `data = null`
- **错误码**: 404-角色不存在

### 3.3 分配用户角色
- **URL**: `POST /api/admin/v1/users/{userId}/roles`

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| roleIds | Long[] | 是 | 角色ID列表 |

- **响应**: `data = null`
- **错误码**: 404-用户不存在

---

## 四、日志接口（管理员）

### 4.1 日志查询
- **URL**: `GET /api/admin/v1/logs`

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| userId | Long | 否 | 操作人ID |
| module | String | 否 | 模块名 |
| startTime | String | 否 | 开始时间 |
| endTime | String | 否 | 结束时间 |
| pageNum | Integer | 否 | 页码 |
| pageSize | Integer | 否 | 每页条数 |

- **响应**: `data = PageResult<LogVo>`

---

## 五、配置接口（管理员）

### 5.1 配置列表
- **URL**: `GET /api/admin/v1/configs`

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| pageNum | Integer | 否 | 页码 |
| pageSize | Integer | 否 | 每页条数 |

- **响应**: `data = PageResult<ConfigVo>`

### 5.2 更新配置
- **URL**: `PUT /api/admin/v1/configs/{key}`

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| configValue | String | 是 | 配置值 |

- **响应**: `data = null`
- **错误码**: 404-配置不存在

---

**文档版本**: v1.3.0
**最后更新**: 2026-06-28
