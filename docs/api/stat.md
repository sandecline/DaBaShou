# 数据统计模块 API契约

## 模块信息
- **模块**: dabashou-stat
- **前缀**: `/api/v1/stats`, `/api/admin/v1/stats`

---

## 一、个人统计接口

### 1.1 个人概览
- **URL**: `GET /api/v1/stats/overview`
- **响应**:
```json
{
  "totalOrders": 50,
  "completedOrders": 45,
  "totalIncome": 2000,
  "totalExpense": 500,
  "trustScore": 4.5,
  "skillCount": 5,
  "reviewCount": 30
}
```

### 1.2 我的订单趋势
- **URL**: `GET /api/v1/stats/orders/trend`

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| days | Integer | 否 | 天数，默认30 |

- **响应**:
```json
[
  { "date": "2026-07-01", "value": 5 },
  { "date": "2026-07-02", "value": 3 }
]
```

### 1.3 我的积分趋势
- **URL**: `GET /api/v1/stats/points/trend`

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| days | Integer | 否 | 天数，默认30 |

- **响应**: 同1.2

### 1.4 技能热度排行
- **URL**: `GET /api/v1/stats/skills/heat`

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| limit | Integer | 否 | 数量，默认10 |

- **响应**:
```json
[
  {
    "skillTagId": 1,
    "skillTagName": "Java开发",
    "shelfCount": 20,
    "demandCount": 15,
    "orderCount": 30,
    "heatScore": 95.5
  }
]
```

### 1.5 分类占比
- **URL**: `GET /api/v1/stats/categories`
- **响应**:
```json
[
  {
    "categoryName": "编程开发",
    "count": 50,
    "percentage": 35.5
  }
]
```

---

## 二、平台统计接口（管理员）

### 2.1 平台总览
- **URL**: `GET /api/admin/v1/stats/overview`
- **响应**:
```json
{
  "totalUsers": 1000,
  "totalOrders": 5000,
  "totalShelves": 800,
  "totalDemands": 300,
  "todayNewUsers": 20,
  "todayNewOrders": 50
}
```

### 2.2 每日趋势
- **URL**: `GET /api/admin/v1/stats/daily-trend`

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| days | Integer | 否 | 天数，默认30 |

- **响应**:
```json
[
  {
    "date": "2026-07-01",
    "newUserCount": 20,
    "activeUserCount": 500,
    "newOrderCount": 50,
    "completedOrderCount": 45,
    "pointInflow": 10000,
    "pointOutflow": 8000
  }
]
```

### 2.3 用户活跃度
- **URL**: `GET /api/admin/v1/stats/user-active`

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| days | Integer | 否 | 天数，默认30 |

- **响应**:
```json
[
  { "date": "2026-07-01", "activeUsers": 500, "newUsers": 20 }
]
```

### 2.4 信任分分布
- **URL**: `GET /api/admin/v1/stats/trust-distribution`
- **响应**:
```json
[
  { "level": "新人", "count": 200, "percentage": 20.0 },
  { "level": "靠谱", "count": 600, "percentage": 60.0 },
  { "level": "金牌", "count": 200, "percentage": 20.0 }
]
```

### 2.5 数据导出
- **URL**: `GET /api/admin/v1/stats/export`

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| type | String | 是 | 导出类型(users/orders/points) |

- **响应**: 文件流(Content-Type: application/octet-stream)
- **错误码**: 400-类型不支持

---

**文档版本**: v1.3.0
**最后更新**: 2026-06-28
