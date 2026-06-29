# 信用评价模块 API契约

## 模块信息
- **模块**: dabashou-credit
- **前缀**: `/api/v1/reviews`, `/api/v1/violations`, `/api/v1/appeals`

---

## 一、评价接口

### 1.1 提交评价
- **URL**: `POST /api/v1/reviews`

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| orderId | Long | 是 | 订单ID |
| rating | Integer | 是 | 评分1-5 |
| content | String | 否 | 评价内容 |
| images | String[] | 否 | 评价图片URL |
| isAnonymous | Boolean | 否 | 是否匿名 |

- **响应**: `data = 1`（评价ID，Long）
- **错误码**: 400-订单未完成, 409-已评价

### 1.2 我发出的评价
- **URL**: `GET /api/v1/reviews/mine`

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| pageNum | Integer | 否 | 页码 |
| pageSize | Integer | 否 | 每页条数 |

- **响应**: `data = PageResult<ReviewVo>`

### 1.3 我收到的评价
- **URL**: `GET /api/v1/reviews/received`

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| pageNum | Integer | 否 | 页码 |
| pageSize | Integer | 否 | 每页条数 |

- **响应**: `data = PageResult<ReviewVo>`

### 1.4 订单评价查询
- **URL**: `GET /api/v1/orders/{orderId}/review`
- **响应**: `data = ReviewVo`
- **错误码**: 404-未评价

---

## 二、违规举报接口

### 2.1 举报违规
- **URL**: `POST /api/v1/violations`

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| targetUserId | Long | 是 | 被举报用户ID |
| orderId | Long | 否 | 关联订单ID |
| type | Integer | 是 | 违规类型 |
| reason | String | 是 | 举报原因 |
| evidence | String[] | 否 | 证据图片URL |

- **响应**: `data = 1`（违规记录ID，Long）
- **错误码**: 400-参数错误

### 2.2 我的违规记录
- **URL**: `GET /api/v1/violations/mine`

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| pageNum | Integer | 否 | 页码 |
| pageSize | Integer | 否 | 每页条数 |

- **响应**: `data = PageResult<ViolationVo>`

---

## 三、申诉接口

### 3.1 提交申诉
- **URL**: `POST /api/v1/appeals`

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| violationId | Long | 是 | 违规记录ID |
| reason | String | 是 | 申诉理由 |
| evidence | String[] | 否 | 证据图片URL |

- **响应**: `data = 1`（申诉ID，Long）
- **错误码**: 404-违规不存在, 409-已申诉

### 3.2 我的申诉记录
- **URL**: `GET /api/v1/appeals/mine`

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| pageNum | Integer | 否 | 页码 |
| pageSize | Integer | 否 | 每页条数 |

- **响应**: `data = PageResult<AppealVo>`

---

**文档版本**: v1.3.0
**最后更新**: 2026-06-28
