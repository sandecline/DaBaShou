# 订单模块 API契约

## 模块信息
- **模块**: dabashou-order
- **前缀**: `/api/v1/orders`
- **兼容入口**: `/api/v1/order` 暂时保留，后续版本废弃
- **核心**: 双方核销与积分担保；旧状态 2、4 仅为兼容保留
- **鉴权**: 除管理员仲裁外，均须登录；订单操作还会校验参与者身份

---

## 一、订单状态机

| 状态码 | 状态名 | 允许的下一状态 |
|--------|--------|----------------|
| 0 | 已取消 | 无（终态） |
| 1 | 待核销 | → 3(服务中), 0(已取消) |
| 2 | 已支付(担保中) | 旧流程保留，不用于新订单 |
| 3 | 服务中 | → 5(已完成), 6(已退款), 7(争议中), 0(已取消) |
| 4 | 待确认 | 旧流程保留，不用于新订单 |
| 5 | 已完成 | → 7(争议中) |
| 6 | 已退款 | → 0(已取消) |
| 7 | 争议中 | → 5(已完成), 6(已退款) |

---

## 二、订单接口

### 2.1 创建订单（从货架）
- **URL**: `POST /api/v1/orders/from-shelf`
- **请求体**: `idempotentToken` 必填

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| shelfId | Long | 是 | 技能货架ID（兼容 `skillShelfId`） |
| timeSlotId | Long | 否 | 时间格子ID |
| remark | String | 否 | 备注 |
| idempotentToken | String | 是 | 幂等令牌 |

```json
{
  "shelfId": 1,
  "timeSlotId": 1,
  "remark": "string",
  "idempotentToken": "order-create-001"
}
```
- **响应**: `data = 1`（订单ID，Long）
- **状态**: 1(待核销)
- **错误码**: 400-参数错误, 404-货架不存在, 409-货架已下架

### 2.2 创建订单（从需求）
- **URL**: `POST /api/v1/orders/from-demand`
- **请求体**: `idempotentToken` 必填

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| demandId | Long | 是 | 需求ID |
| shelfId | Long | 否 | 可选货架ID；不传时按需求本身生成订单 |
| remark | String | 否 | 备注 |
| idempotentToken | String | 是 | 幂等令牌 |

```json
{
  "demandId": 1,
  "shelfId": 1,
  "remark": "string",
  "idempotentToken": "demand-create-001"
}
```
- **响应**: `data = 1`（订单ID，Long）
- **错误码**: 400-参数错误, 404-需求不存在, 409-需求已关闭

### 2.3 订单列表
- **URL**: `GET /api/v1/orders`

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| role | String | 否 | buyer/seller，默认全部 |
| status | Integer[] | 否 | 状态码筛选，逗号分隔，如 `1,3` |
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
  "statusName": "待核销",
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
- **响应**: `{ "code": 200, "msg": "success", "data": { "status": 1, "statusName": "待核销" } }`
- **错误码**: 404-订单不存在

---

## 三、订单操作接口

### 3.1 旧支付接口（已废弃）
- **URL**: `POST /api/v1/orders/{id}/pay`
- **业务**: 仅供旧客户端兼容；新订单使用双方核销流程，不再调用支付接口
- **错误码**: 409-新订单状态不允许走旧支付流程

### 3.2 取消订单（→0）
- **URL**: `POST /api/v1/orders/{id}/cancel`

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| reason | String | 否 | 取消原因 |

- **响应**: `data = null`
- **业务**: 待核销或服务中订单可取消；服务中取消会释放冻结积分
- **错误码**: 409-状态不允许

### 3.3 获取旧核销码（兼容入口）
- **URL**: `GET /api/v1/orders/{id}/verify-code`
- **响应**: `{ "verifyCode": "A1B2C3", "expireTime": "2026-07-01 10:30:00" }`
- **说明**: 新订单的双方核销码在订单详情中按身份返回

### 3.4 刷新旧核销码（兼容入口）
- **URL**: `PUT /api/v1/orders/{id}/verify-code`
- **响应**: `{ "verifyCode": "D4E5F6", "expireTime": "2026-07-01 11:00:00" }`
- **业务**: 重新生成核销码，重置30分钟TTL
- **错误码**: 409-状态不允许

### 3.5 双方核销订单（1→3→5）
- **URL**: `POST /api/v1/orders/{id}/verify`
- **业务**: `start` 阶段双方输入对方的开始核销码；双方核销完成后冻结积分并进入服务中。`complete` 阶段双方输入对方的完成确认码；双方确认完成后结算积分。

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| phase | String | 是 | `start` 或 `complete` |
| code | String | 是 | 对方的核销码或确认码 |

```json
{"phase":"start","code":"A1B2C3"}
```

- **响应**: `{ "code": 200, "msg": "success", "data": null }`
- **错误码**: 403-非订单参与者, 409-状态或核销码不正确

### 3.6 发起争议（3→7或5→7）
- **URL**: `POST /api/v1/orders/{id}/dispute`

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| reason | String | 是 | 争议原因 |
| explain | String | 否 | 争议补充说明 |

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

## 四、定时处理

当前定时任务每小时检查旧流程中状态为 4 的订单，超过 `order.confirm_timeout_hours` 配置后自动结算；默认值为 72 小时。新流程按双方核销码确认，当前没有待核销自动取消或核销码超时自动退款任务。

---

## 五、DTO/VO定义

### CreateOrderDto
```java
public class CreateOrderDto {
    @NotNull private Long shelfId;
    private Long timeSlotId;
    private String remark;
    @NotBlank private String idempotentToken;
}
```

### CreateOrderFromDemandDto
```java
public class CreateOrderFromDemandDto {
    @NotNull private Long demandId;
    private Long shelfId;
    private String remark;
    @NotBlank private String idempotentToken;
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
    private String explain;
}
```

### VerifyDto
```java
public class VerifyDto {
    @NotBlank private String code;
    @NotBlank private String phase;
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
    private String tagName;
    private Integer pointAmount;
    private Integer status;
    private String statusName;
    private Long buyerId;
    private String buyerNickname;
    private Long sellerId;
    private String sellerNickname;
    private LocalDateTime createTime;
}
```

---

**文档版本**: v1.4.0
**最后更新**: 2026-09-18

---

## 2026-07-01 补充：接单防重复

- `POST /api/v1/orders/from-demand` 成功创建订单后，服务端会在同一事务内将对应 `dbs_demand.status` 从 `1` 更新为 `2`。
- 同一需求被再次用于创建订单时返回 `409`，避免原需求详情链接反复接单。
- `shelfId` 可选；未提供可用货架时，订单标题、标签和积分按需求本身生成，接单方取当前登录用户。
- `POST /api/v1/orders/from-shelf` 成功创建订单后，服务端会在同一事务内将对应 `dbs_skill_shelf.status` 从 `1` 更新为 `0`。
- 同一服务被再次用于创建订单时返回 `409`，避免原服务详情链接反复接取。

