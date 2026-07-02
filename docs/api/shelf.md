# 技能货架模块 API契约

## 模块信息
- **模块**: dabashou-shelf
- **前缀**: `/api/v1/shelves`

---

## 一、服务发布接口

### 1.1 发布服务
- **URL**: `POST /api/v1/shelves`

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| skillTagId | Long | 是 | 技能标签ID |
| title | String | 是 | 服务标题 |
| description | String | 否 | 服务描述 |
| pointPrice | Integer | 是 | 积分价格 |
| durationMinutes | Integer | 是 | 服务时长(分钟) |
| locationType | Integer | 是 | 1-线上 2-线下 3-均可 |

- **响应**: `data = 1`（货架ID，Long）
- **错误码**: 400-参数错误

### 1.2 编辑服务
- **URL**: `PUT /api/v1/shelves/{id}`
- **请求体**: 同1.1（所有字段可选）
- **响应**: `data = null`
- **错误码**: 403-非本人, 404-不存在

### 1.3 上架服务
- **URL**: `PUT /api/v1/shelves/{id}/on`
- **响应**: `data = null`
- **错误码**: 403-非本人, 409-已上架

### 1.4 下架服务
- **URL**: `PUT /api/v1/shelves/{id}/off`
- **响应**: `data = null`
- **错误码**: 403-非本人, 409-已下架

### 1.5 删除服务
- **URL**: `DELETE /api/v1/shelves/{id}`
- **响应**: `data = null`
- **错误码**: 403-非本人, 409-有进行中订单

### 1.6 获取服务详情
- **URL**: `GET /api/v1/shelves/{id}`
- **响应**:
```json
{
  "id": 1,
  "userId": 1,
  "nickname": "张三",
  "avatar": "url",
  "trustScore": 4.5,
  "skillTagName": "Java开发",
  "title": "Java开发辅导",
  "description": "string",
  "pointPrice": 50,
  "durationMinutes": 60,
  "locationType": 3,
  "locationTypeDesc": "线上/线下",
  "status": 1,
  "statusDesc": "上架中",
  "createTime": "2026-01-01 00:00:00"
}
```

---

## 二、服务搜索接口

### 2.1 搜索服务列表
- **URL**: `GET /api/v1/shelves`

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| keyword | String | 否 | 关键词 |
| categoryId | Long | 否 | 分类ID |
| skillTagId | Long | 否 | 技能标签ID |
| locationType | Integer | 否 | 位置类型 |
| sortBy | String | 否 | heat/distance/trust/price |
| longitude | BigDecimal | 否 | 经度(距离排序时必填) |
| latitude | BigDecimal | 否 | 纬度(距离排序时必填) |
| pageNum | Integer | 否 | 页码 |
| pageSize | Integer | 否 | 每页条数 |

- **响应**: `data = PageResult<ShelfItemVo>`

### 2.2 我发布的服务
- **URL**: `GET /api/v1/shelves/mine`

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| pageNum | Integer | 否 | 页码 |
| pageSize | Integer | 否 | 每页条数 |

- **响应**: `data = PageResult<ShelfItemVo>`

### 2.3 用户小铺页
- **URL**: `GET /api/v1/shelves/users/{userId}/shelves`

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| pageNum | Integer | 否 | 页码 |
| pageSize | Integer | 否 | 每页条数 |

- **响应**: `data = PageResult<ShelfItemVo>`

---

## 三、时间格子接口

### 3.1 批量设置时间格子
- **URL**: `POST /api/v1/shelves/{id}/timeslots`

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| dayOfWeek | Integer | 是 | 星期几 1-7 |
| startTime | String | 是 | 开始时间 HH:mm |
| endTime | String | 是 | 结束时间 HH:mm |

- **请求体**: `[{ "dayOfWeek": 1, "startTime": "09:00", "endTime": "10:00" }]`
- **响应**: `data = null`
- **错误码**: 403-非本人

### 3.2 查询时间格子
- **URL**: `GET /api/v1/shelves/{id}/timeslots`
- **响应**:
```json
[
  {
    "id": 1,
    "dayOfWeek": 1,
    "startTime": "09:00",
    "endTime": "10:00",
    "available": true
  }
]
```

### 3.3 删除时间格子
- **URL**: `DELETE /api/v1/shelves/{shelfId}/timeslots/{slotId}`
- **响应**: `data = null`
- **错误码**: 403-非本人, 404-不存在

---

## 四、DTO/VO定义

### SkillShelfDto
```java
public class SkillShelfDto {
    @NotNull private Long skillTagId;
    @NotBlank private String title;
    private String description;
    @NotNull private Integer pointPrice;
    @NotNull private Integer durationMinutes;
    @NotNull private Integer locationType;  // 1-线上 2-线下 3-均可
}
```

### ShelfItemVo
```java
public class ShelfItemVo {
    private Long id;
    private Long userId;
    private String nickname;
    private String avatar;
    private BigDecimal trustScore;
    private String skillTagName;
    private String title;
    private Integer pointPrice;
    private Integer durationMinutes;
    private Integer locationType;
    private Integer status;
}
```

### ShelfDetailVo
```java
public class ShelfDetailVo extends ShelfItemVo {
    private String description;
    private String locationTypeDesc;
    private String statusDesc;
    private LocalDateTime createTime;
}
```

### TimeSlotDto
```java
public class TimeSlotDto {
    @NotNull private Integer dayOfWeek;  // 1-7
    @NotBlank private String startTime;  // HH:mm
    @NotBlank private String endTime;    // HH:mm
}
```

---

**文档版本**: v1.3.0
**最后更新**: 2026-07-01
