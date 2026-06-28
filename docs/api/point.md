# 积分模块 API契约

## 模块信息
- **模块**: dabashou-point
- **前缀**: `/api/v1/points`

---

## 一、余额接口

### 1.1 查询积分余额
- **URL**: `GET /api/v1/points/balance`
- **响应**:
```json
{
  "available": 100,
  "frozen": 50,
  "total": 150
}
```

---

## 二、流水接口

### 2.1 流水分页查询
- **URL**: `GET /api/v1/points/transactions`

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| type | Integer | 否 | 流水类型1-6 |
| orderId | Long | 否 | 关联订单ID |
| startDate | String | 否 | 开始日期 |
| endDate | String | 否 | 结束日期 |
| pageNum | Integer | 否 | 页码 |
| pageSize | Integer | 否 | 每页条数 |

- **响应**: `data = PageResult<PointTransVo>`

### 2.2 流水详情
- **URL**: `GET /api/v1/points/transactions/{id}`
- **响应**:
```json
{
  "id": 1,
  "type": 1,
  "typeDesc": "收入",
  "amount": 50,
  "balanceAfter": 150,
  "orderId": 1,
  "orderNo": "ORD20260701001",
  "description": "订单结算",
  "createTime": "2026-01-01 00:00:00"
}
```
- **错误码**: 404-不存在

### 2.3 收支统计
- **URL**: `GET /api/v1/points/stats`
- **响应**:
```json
{
  "totalIncome": 500,
  "totalExpense": 200,
  "monthIncome": 100,
  "monthExpense": 50
}
```

---

## 三、签到接口

### 3.1 每日签到
- **URL**: `POST /api/v1/points/sign-in`
- **响应**:
```json
{
  "reward": 5,
  "consecutiveDays": 3
}
```
- **错误码**: 409-今日已签到
- **防刷**: 同一用户每日限1次

### 3.2 签到状态
- **URL**: `GET /api/v1/points/sign-in/status`
- **响应**:
```json
{
  "todaySigned": false,
  "consecutiveDays": 3,
  "reward": 5
}
```

---

## 四、担保池接口

### 4.1 查询担保池
- **URL**: `GET /api/v1/points/guarantee-pool`
- **响应**:
```json
{
  "totalPool": 10000,
  "frozenAmount": 5000,
  "availableAmount": 5000
}
```

---

## 五、积分流水类型

| 类型码 | 说明 |
|--------|------|
| 1 | 收入（订单结算） |
| 2 | 支出（下单支付） |
| 3 | 冻结（订单担保） |
| 4 | 解冻（订单取消退款） |
| 5 | 签到奖励 |
| 6 | 系统调整 |

---

**文档版本**: v1.3.0
**最后更新**: 2026-06-28
