# 订单模块 API契约

## 模块信息
- **模块**: dabashou-order
- **前缀**: `/api/v1/orders`
- **兼容入口**: `/api/v1/order` 暂时保留，后续版本废弃
- **核心**: 8状态机硬流转

---

## 一、订单状态机

| 状态码 | 状态名 | 允许的下一状态 |
|--------|--------|----------------|
| 0 | 已取消 | 无（终态） |
| 1 | 待支付 | → 2(已支付), 0(已取消) |
| 2 | 已支付(担保中) | → 3(服务中), 6(已退款), 0(已取消) |
| 3 | 服务中 | → 4(待确认), 6(已退款), 0(已取消) |
| 4 | 待确认 | → 5(已完成), 7(争议中), 6(已退款) |
| 5 | 已完成 | 无（终态） |
| 6 | 已退款 | 无（终态） |
| 7 | 争议中 | → 5(已完成), 6(已退款) |

---

## 二、订单接口

### 2.1 创建订单（从货架）
- **URL**: `POST /api/v1/orders/from-shelf`
- **请求头**: `X-Idempotent-Token: {uuid}`

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| skillShelfId | Long | 是 | 技能货架ID |
| timeSlotId | Long | 否 | 时间格子ID |
| remark | String | 否 | 备注 |

```json
{
  "skillShelfId": 1,
  "timeSlotId": 1,
  "remark": "string"
}
```
- **响应**: `data = 1`（订单ID，Long）
- **状态**: 1(待支付)
- **错误码**: 400-参数错误, 404-货架不存在, 409-货架已下架

### 2.2 创建订单（从需求）
- **URL**: `POST /api/v1/orders/from-demand`
- **请求头**: `X-Idempotent-Token: {uuid}`

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| demandId | Long | 是 | 需求ID |
| sellerId | Long | 是 | 卖家用户ID |
| remark | String | 否 | 备注 |

```json
{
  "demandId": 1,
  "sellerId": 2,
  "remark": "string"
}
```
- **响应**: `data = 1`（订单ID，Long）
- **错误码**: 400-参数错误, 404-需求不存在, 409-需求已关闭

### 2.3 订单列表
- **URL**: `GET /api/v1/orders`

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| role | String | 否 | buyer/seller，默认全部 |
| status | Integer | 否 | 状态码筛选 |
| pageNum | Integer | 否 | 页码，默认1 |
| pageSize | Integer | 否 | 每页条数，默认10 |

- **响应**: `data = PageResult<OrderItemVo>`
- **错误码**: 无

### 2.4 订单详情
- **URL**: `GET /api/v1/orders/{id}`
- **响应**:
```json
{
  "id": 1,
  "orderNo": "ORD20260701001",
  "buyerId": 1,
  "buyerNickname": "张三",
  "sellerId": 2,
  "sellerNickname": "李四",
  "title": "Java开发辅导",
  "skillTagName": "Java开发",
  "pointAmount": 50,
  "status": 1,
  "statusDesc": "待支付",
  "verifyCode": null,
  "verifyCodeExpire": null,
  "serviceStartTime": null,
  "serviceEndTime": null,
  "completeTime": null,
  "cancelTime": null,
  "cancelReason": null,
  "remark": "string",
  "createTime": "2026-07-01 10:00:00"
}
```
- **错误码**: 404-订单不存在, 403-无权查看

### 2.5 查询订单状态
- **URL**: `GET /api/v1/orders/{id}/status`
- **响应**: `data = { "status": 1, "statusDesc": "待支付" }`
- **错误码**: 404-订单不存在

---

## 三、订单操作接口

### 3.1 支付订单（1→2）
- **URL**: `POST /api/v1/orders/{id}/pay`
- **请求头**: `X-Idempotent-Token: {uuid}`
- **响应**:
```json
{
  "status": 2,
  "verifyCode": "A1B2C3",
  "verifyCodeExpire": "2026-07-01 10:30:00"
}
```
- **业务**: 冻结积分, 生成6位核销码(Redis, 30分钟TTL)
- **错误码**: 400-积分不足, 409-状态不允许, 429-重复提交

### 3.2 取消订单（→0）
- **URL**: `POST /api/v1/orders/{id}/cancel`

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| reason | String | 否 | 取消原因 |

- **响应**: `data = null`
- **业务**:
  - 1→0: 无扣分
  - 2→0: 买家取消扣10%, 卖家取消扣20%
- **错误码**: 409-状态不允许

### 3.3 开始服务（2→3）
- **URL**: `POST /api/v1/orders/{id}/start`
- **响应**: `data = null`
- **角色**: 卖家
- **错误码**: 403-非卖家, 409-状态不允许

### 3.4 获取核销码
- **URL**: `GET /api/v1/orders/{id}/verify-code`
- **响应**: `{ "verifyCode": "A1B2C3", "expireTime": "2026-07-01 10:30:00" }`
- **错误码**: 409-状态不允许

### 3.5 刷新核销码
- **URL**: `PUT /api/v1/orders/{id}/verify-code`
- **响应**: `{ "verifyCode": "D4E5F6", "expireTime": "2026-07-01 11:00:00" }`
- **业务**: 重新生成核销码，重置30分钟TTL
- **错误码**: 409-状态不允许

### 3.6 核销订单（3→4）
- **URL**: `POST /api/v1/orders/{id}/verify`
- **请求头**: `X-Idempotent-Token: {uuid}`

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| verifyCode | String | 是 | 6位核销码 |

- **响应**: `data = null`
- **错误码**: 400-核销码错误, 409-已过期, 429-重复提交

### 3.7 买家确认（4→5）
- **URL**: `POST /api/v1/orders/{id}/confirm`
- **响应**: `data = null`
- **业务**: 结算积分给卖家
- **错误码**: 403-非买家, 409-状态不允许

### 3.8 发起争议（4→7）
- **URL**: `POST /api/v1/orders/{id}/dispute`

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| reason | String | 是 | 争议原因 |

- **响应**: `data = null`
- **错误码**: 403-非买家, 409-状态不允许

### 3.9 仲裁订单（7→5或7→6）
- **URL**: `POST /api/v1/orders/{id}/arbitrate`
- **请求头**: `X-Idempotent-Token: {uuid}`

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| result | String | 是 | complete(完成) / refund(退款) |
| reason | String | 是 | 仲裁理由 |
| refundAmount | Integer | 否 | 退款积分数(退款时可指定) |

- **响应**: `data = null`
- **角色**: 管理员
- **错误码**: 409-状态不允许, 429-重复提交

### 3.10 退款（→6）
- **URL**: `POST /api/v1/orders/{id}/refund`

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| reason | String | 是 | 退款原因 |

- **响应**: `data = null`
- **错误码**: 409-状态不允许

---

## 四、超时熔断

| 触发条件 | 动作 |
|----------|------|
| 待支付15分钟 | 自动取消(1→0) |
| 核销码30分钟 | 自动退款(3→6) |
| 待确认7天 | 自动完成(4→5) |

---

## 五、DTO/VO定义

### CreateOrderFromShelfDto
```java
public class CreateOrderFromShelfDto {
    @NotNull private Long skillShelfId;
    private Long timeSlotId;
    private String remark;
}
```

### CreateOrderFromDemandDto
```java
public class CreateOrderFromDemandDto {
    @NotNull private Long demandId;
    @NotNull private Long sellerId;
    private String remark;
}
```

### CancelDto
```java
public class CancelDto {
    private String reason;
}
```

### DisputeDto
```java
public class DisputeDto {
    @NotBlank private String reason;
}
```

### VerifyDto
```java
public class VerifyDto {
    @NotBlank private String verifyCode;
}
```

### ArbitrateDto
```java
public class ArbitrateDto {
    @NotBlank private String result;
    @NotBlank private String reason;
    private Integer refundAmount;
}
```

### RefundDto
```java
public class RefundDto {
    @NotBlank private String reason;
}
```

### OrderItemVo
```java
public class OrderItemVo {
    private Long id;
    private String orderNo;
    private String title;
    private String skillTagName;
    private Integer pointAmount;
    private Integer status;
    private String statusDesc;
    private String counterpartNickname;
    private String counterpartAvatar;
    private LocalDateTime createTime;
}
```

---

**文档版本**: v1.3.0
**最后更新**: 2026-07-01

---

## 2026-07-01 补充：接单防重复

- `POST /api/v1/orders/from-demand` 成功创建订单后，服务端会在同一事务内将对应 `dbs_demand.status` 从 `1` 更新为 `2`。
- 同一需求被再次用于创建订单时返回 `409`，避免原需求详情链接反复接单。
- `shelfId` 可选；未提供可用货架时，订单标题、标签和积分按需求本身生成，接单方取当前登录用户。
- `POST /api/v1/orders/from-shelf` 成功创建订单后，服务端会在同一事务内将对应 `dbs_skill_shelf.status` 从 `1` 更新为 `0`。
- 同一服务被再次用于创建订单时返回 `409`，避免原服务详情链接反复接取。
