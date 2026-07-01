# 用户与认证模块 API契约

## 模块信息
- **模块**: dabashou-user
- **前缀**: `/api/v1/auth`, `/api/v1/user`, `/api/v1/users`
- **依赖**: dabashou-common, dabashou-system

---

## 一、认证接口

### 1.1 用户注册
- **URL**: `POST /api/v1/auth/register`
- **认证**: 无需

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| username | String | 是 | 5-50字符 |
| password | String | 是 | 6-20字符 |
| nickname | String | 否 | 默认=username |
| phone | String | 否 | 手机号 |

- **响应**: `data = null`
- **错误码**: 400-参数错误, 409-用户名已存在

### 1.2 密码登录
- **URL**: `POST /api/v1/auth/login`
- **认证**: 无需

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| username | String | 是 | 用户名 |
| password | String | 是 | 密码 |

- **响应**:
```json
{
  "accessToken": "string",
  "refreshToken": "string",
  "expiresIn": 86400,
  "userId": 1,
  "nickname": "string",
  "avatar": "string"
}
```

### 1.3 发送短信验证码
- **URL**: `POST /api/v1/auth/sms-code`
- **认证**: 无需

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| phone | String | 是 | 手机号 |

- **响应**: `data = null`
- **限流**: 同一手机号60秒内只能发送1次
- **错误码**: 429-发送过于频繁

### 1.4 短信验证码登录
- **URL**: `POST /api/v1/auth/sms-login`
- **认证**: 无需

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| phone | String | 是 | 手机号 |
| code | String | 是 | 6位验证码 |

- **响应**: 同1.2
- **错误码**: 400-验证码错误, 401-验证码过期

### 1.5 刷新Token
- **URL**: `POST /api/v1/auth/refresh`
- **认证**: 无需

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| refreshToken | String | 是 | 刷新令牌 |

- **响应**: 同1.2
- **错误码**: 401-refreshToken过期

### 1.6 登出
- **URL**: `POST /api/v1/auth/logout`
- **认证**: 需要
- **响应**: `data = null`

---

## 二、用户资料接口

### 2.1 获取个人信息
- **URL**: `GET /api/v1/user/profile`
- **响应**:
```json
{
  "id": 1,
  "username": "string",
  "nickname": "string",
  "avatar": "string (URL)",
  "phone": "138****1234",
  "email": "string",
  "pointBalance": 100,
  "trustScore": 4.5,
  "trustLevel": "靠谱",
  "longitude": 116.1234567,
  "latitude": 39.1234567,
  "campus": "string",
  "building": "string",
  "bio": "string",
  "status": 1,
  "createTime": "2026-01-01 00:00:00"
}
```

### 2.2 更新个人信息
- **URL**: `PUT /api/v1/user/profile`

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| nickname | String | 否 | 昵称 |
| avatar | String | 否 | 头像URL |
| bio | String | 否 | 简介，最长500字 |
| campus | String | 否 | 校区 |
| building | String | 否 | 楼栋 |

- **响应**: `data = null`

### 2.3 修改密码
- **URL**: `PUT /api/v1/user/password`

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| oldPassword | String | 是 | 原密码 |
| newPassword | String | 是 | 6-20字符 |

- **响应**: `data = null`
- **错误码**: 400-原密码错误

### 2.4 更新位置
- **URL**: `PUT /api/v1/user/location`

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| longitude | BigDecimal | 是 | 经度 |
| latitude | BigDecimal | 是 | 纬度 |

- **响应**: `data = null`

### 2.5 公开脱敏用户详情
- **URL**: `GET /api/v1/users/{userId}`
- **认证**: 无需

- **响应**:
```json
{
  "id": 1,
  "nickname": "张三",
  "avatar": "string (URL)",
  "trustScore": 4.5,
  "trustLevel": "金牌",
  "campus": "主校区",
  "building": "一号楼",
  "bio": "string",
  "status": 1,
  "createTime": "2026-01-01 00:00:00"
}
```
- **脱敏规则**: 不返回用户名、手机号、邮箱、经纬度、积分余额等个人敏感字段。
- **错误码**: 404-用户不存在或已禁用

---

## 三、校园认证接口

### 3.1 提交校园认证
- **URL**: `POST /api/v1/user/campus-auth`

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| authType | String | 是 | student/teacher |
| studentNo | String | 是 | 学号 |
| realName | String | 是 | 真实姓名 |
| campus | String | 是 | 校区 |
| college | String | 否 | 学院 |
| credentialFileId | Long | 否 | 凭证文件ID |

- **响应**: `data = null`
- **错误码**: 409-已提交认证

### 3.2 查询认证状态
- **URL**: `GET /api/v1/user/campus-auth`
- **响应**:
```json
{
  "id": 1,
  "authType": "student",
  "studentNo": "2024001",
  "realName": "张三",
  "campus": "主校区",
  "college": "计算机学院",
  "status": 0,
  "statusDesc": "待审核",
  "reviewRemark": null,
  "reviewTime": null,
  "createTime": "2026-01-01 00:00:00"
}
```

---

## 四、信任分接口

### 4.1 查询信任分
- **URL**: `GET /api/v1/user/trust-score`
- **响应**:
```json
{
  "score": 4.5,
  "level": "靠谱",
  "recentLogs": [
    {
      "type": "order_complete",
      "scoreChange": 0.1,
      "scoreBefore": 4.4,
      "scoreAfter": 4.5,
      "reason": "订单完成",
      "createTime": "2026-01-01 00:00:00"
    }
  ]
}
```

---

## 五、DTO/VO定义

### RegisterDto
```java
public class RegisterDto {
    @NotBlank @Size(min=5, max=50) private String username;
    @NotBlank @Size(min=6, max=20) private String password;
    private String nickname;
    private String phone;
}
```

### LoginDto
```java
public class LoginDto {
    @NotBlank private String username;
    @NotBlank private String password;
}
```

### LoginVo
```java
public class LoginVo {
    private String accessToken;
    private String refreshToken;
    private Long expiresIn;   // 单位: 秒
    private Long userId;
    private String nickname;
    private String avatar;
}
```

### SmsLoginDto
```java
public class SmsLoginDto {
    @NotBlank private String phone;
    @NotBlank @Size(min=6, max=6) private String code;
}
```

### TrustScoreVo
```java
public class TrustScoreVo {
    private BigDecimal score;
    private String level;
    private List<TrustLogItem> recentLogs;
}
public class TrustLogItem {
    private String type;
    private BigDecimal scoreChange;
    private BigDecimal scoreBefore;
    private BigDecimal scoreAfter;
    private String reason;
    private LocalDateTime createTime;
}
```

---

**文档版本**: v1.3.0
**最后更新**: 2026-07-01
