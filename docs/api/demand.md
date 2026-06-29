# 需求模块 API契约

## 模块信息
- **模块**: dabashou-demand
- **前缀**: `/api/v1/demands`

---

## 一、需求接口

### 1.1 发布需求
- **URL**: `POST /api/v1/demands`

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| skillTagId | Long | 是 | 技能标签ID |
| title | String | 是 | 需求标题 |
| description | String | 否 | 需求描述 |
| pointReward | Integer | 否 | 悬赏积分 |
| deadline | String | 否 | 截止时间 |
| locationType | Integer | 是 | 1-线上 2-线下 3-均可 |
| demandType | Integer | 否 | 1-求助悬赏(默认) 2-技能置换 |
| longitude | BigDecimal | 否 | 经度 |
| latitude | BigDecimal | 否 | 纬度 |

- **响应**: `data = 1`（需求ID，Long）
- **错误码**: 400-参数错误

### 1.2 编辑需求
- **URL**: `PUT /api/v1/demands/{id}`
- **请求体**: 同1.1（所有字段可选）
- **响应**: `data = null`
- **错误码**: 403-非本人, 409-状态不允许

### 1.3 关闭需求
- **URL**: `PUT /api/v1/demands/{id}/close`
- **响应**: `data = null`
- **错误码**: 403-非本人, 409-已关闭

### 1.4 删除需求
- **URL**: `DELETE /api/v1/demands/{id}`
- **响应**: `data = null`
- **错误码**: 403-非本人, 409-有进行中订单

### 1.5 需求详情
- **URL**: `GET /api/v1/demands/{id}`
- **响应**:
```json
{
  "id": 1,
  "userId": 1,
  "nickname": "张三",
  "avatar": "url",
  "skillTagName": "Java开发",
  "title": "需要Java辅导",
  "description": "string",
  "pointReward": 100,
  "deadline": "2026-07-10 18:00:00",
  "locationType": 2,
  "demandType": 1,
  "demandTypeDesc": "求助悬赏",
  "status": 1,
  "statusDesc": "开放中",
  "createTime": "2026-01-01 00:00:00"
}
```

---

## 二、看板接口

### 2.1 需求看板列表
- **URL**: `GET /api/v1/demands`

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| keyword | String | 否 | 关键词 |
| categoryId | Long | 否 | 分类ID |
| skillTagId | Long | 否 | 技能标签ID |
| demandType | Integer | 否 | 需求类型 |
| status | Integer | 否 | 状态筛选 |
| sortBy | String | 否 | time/budget/hot |
| pageNum | Integer | 否 | 页码 |
| pageSize | Integer | 否 | 每页条数 |

- **响应**: `data = PageResult<DemandItemVo>`

### 2.2 我发布的需求
- **URL**: `GET /api/v1/demands/mine`

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| pageNum | Integer | 否 | 页码 |
| pageSize | Integer | 否 | 每页条数 |

- **响应**: `data = PageResult<DemandItemVo>`

---

## 三、揭榜与匹配

### 3.1 揭榜接单
- **URL**: `POST /api/v1/demands/{id}/bid`
- **响应**: `data = null`
- **错误码**: 409-已揭榜, 409-需求已关闭

### 3.2 智能匹配推荐
- **URL**: `GET /api/v1/demands/{id}/match`

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| limit | Integer | 否 | 推荐数量，默认10 |

- **响应**:
```json
[
  {
    "shelfId": 1,
    "userId": 2,
    "nickname": "李四",
    "title": "Java开发辅导",
    "pointPrice": 50,
    "trustScore": 4.5,
    "matchScore": 0.95
  }
]
```

---

## 四、DTO/VO定义

### DemandDto
```java
public class DemandDto {
    @NotNull private Long skillTagId;
    @NotBlank private String title;
    private String description;
    private Integer demandType;      // 1-求助悬赏(默认) 2-技能置换
    private Integer pointReward;
    private List<String> images;
    private String deadline;
    @NotNull private Integer locationType;
    private BigDecimal longitude;
    private BigDecimal latitude;
}
```

### DemandItemVo
```java
public class DemandItemVo {
    private Long id;
    private Long userId;
    private String nickname;
    private String avatar;
    private String skillTagName;
    private String title;
    private Integer pointReward;
    private String deadline;
    private Integer locationType;
    private Integer demandType;
    private String demandTypeDesc;
    private Integer status;
    private String statusDesc;
    private LocalDateTime createTime;
}
```

### DemandMatchVo
```java
public class DemandMatchVo {
    private Long shelfId;
    private Long userId;
    private String nickname;
    private String avatar;
    private String title;
    private Integer pointPrice;
    private BigDecimal trustScore;
    private BigDecimal matchScore;
}
```

---

**文档版本**: v1.3.0
**最后更新**: 2026-06-28
