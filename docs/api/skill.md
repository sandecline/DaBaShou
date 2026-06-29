# 技能模块 API契约

## 模块信息
- **模块**: dabashou-skill
- **前缀**: `/api/v1/skills`

---

## 一、技能分类接口

### 1.1 获取分类树
- **URL**: `GET /api/v1/skills/categories/tree`
- **响应**:
```json
[
  {
    "id": 1,
    "name": "编程开发",
    "icon": "code",
    "sortOrder": 1,
    "children": [
      { "id": 11, "name": "Java", "icon": null, "sortOrder": 1 }
    ]
  }
]
```

---

## 二、技能标签接口

### 2.1 获取标签列表
- **URL**: `GET /api/v1/skills/tags`

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| categoryId | Long | 否 | 分类ID筛选 |

- **响应**:
```json
[
  { "id": 1, "categoryId": 1, "name": "Java开发", "status": 1 }
]
```

### 2.2 获取热门标签
- **URL**: `GET /api/v1/skills/tags/hot`

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| limit | Integer | 否 | 数量，默认10 |

- **响应**: 同2.1

---

## 三、用户技能接口

### 3.1 获取我的技能
- **URL**: `GET /api/v1/users/me/skills`
- **响应**:
```json
[
  {
    "id": 1,
    "skillTagId": 1,
    "skillTagName": "Java开发",
    "categoryName": "编程开发",
    "proficiency": 3,
    "proficiencyDesc": "熟练",
    "description": "3年Java开发经验",
    "createTime": "2026-01-01 00:00:00"
  }
]
```

### 3.2 添加技能
- **URL**: `POST /api/v1/users/me/skills`

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| skillTagId | Long | 是 | 技能标签ID |
| proficiency | Integer | 是 | 熟练度1-5 |
| description | String | 否 | 技能描述 |

- **响应**: `data = { "id": 1 }`
- **错误码**: 409-技能已存在

### 3.3 更新技能
- **URL**: `PUT /api/v1/users/me/skills/{id}`

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| proficiency | Integer | 否 | 熟练度1-5 |
| description | String | 否 | 技能描述 |

- **响应**: `data = null`
- **错误码**: 403-非本人, 404-不存在

### 3.4 删除技能
- **URL**: `DELETE /api/v1/users/me/skills/{id}`
- **响应**: `data = null`
- **错误码**: 403-非本人, 404-不存在

---

**文档版本**: v1.3.0
**最后更新**: 2026-06-28
