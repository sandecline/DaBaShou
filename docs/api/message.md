# 消息模块 API契约

## 模块信息
- **模块**: dabashou-message
- **前缀**: `/api/v1/notifications`, `/api/v1/chat`
- **WebSocket**: `/ws/chat`

---

## 一、通知接口

### 1.1 通知列表
- **URL**: `GET /api/v1/notifications`

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| type | String | 否 | 通知类型 |
| isRead | Boolean | 否 | 已读状态 |
| pageNum | Integer | 否 | 页码 |
| pageSize | Integer | 否 | 每页条数 |

- **响应**: `data = PageResult<NotificationVo>`

### 1.2 未读通知数
- **URL**: `GET /api/v1/notifications/unread-count`
- **响应**: `data = 5`（Integer，未读数量）

### 1.3 标记已读
- **URL**: `PUT /api/v1/notifications/{id}/read`
- **响应**: `data = null`
- **错误码**: 404-不存在

### 1.4 全部已读
- **URL**: `PUT /api/v1/notifications/read-all`
- **响应**: `data = null`

### 1.5 删除通知
- **URL**: `DELETE /api/v1/notifications/{id}`
- **响应**: `data = null`
- **错误码**: 404-不存在

---

## 二、聊天接口

### 2.1 会话列表
- **URL**: `GET /api/v1/chat/sessions`

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| pageNum | Integer | 否 | 页码 |
| pageSize | Integer | 否 | 每页条数 |

- **响应**:
```json
{
  "total": 10,
  "list": [
    {
      "id": 1,
      "otherUserId": 2,
      "otherNickname": "李四",
      "otherAvatar": "url",
      "lastMessage": "你好",
      "lastTime": "2026-01-01 00:00:00",
      "unreadCount": 3
    }
  ]
}
```

### 2.2 聊天记录
- **URL**: `GET /api/v1/chat/sessions/{sessionId}/messages`

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| pageNum | Integer | 否 | 页码 |
| pageSize | Integer | 否 | 每页条数 |

- **响应**: `data = PageResult<ChatMessageVo>`

### 2.3 按对方用户查消息
- **URL**: `GET /api/v1/chat/messages`
- **说明**: 按对方用户ID查询聊天记录，自动查找或创建会话。若会话不存在则返回空列表。

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| targetUserId | Long | 是 | 对方用户ID |
| pageNum | Integer | 否 | 页码 |
| pageSize | Integer | 否 | 每页条数 |

- **响应**: `data = PageResult<ChatMessageVo>`
- **错误码**: 404-对方用户不存在

### 2.4 发起会话
- **URL**: `POST /api/v1/chat/sessions`

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| userId | Long | 是 | 对方用户ID |

- **响应**: `data = 1`（会话ID，Long）
- **错误码**: 404-用户不存在

### 2.5 发送消息（REST）
- **URL**: `POST /api/v1/chat/send`
- **说明**: 通过REST接口发送聊天消息，自动查找或创建会话。

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| receiverId | Long | 是 | 接收方用户ID |
| content | String | 是 | 消息内容 |
| msgType | Integer | 否 | 消息类型：1-文字（默认），2-图片 |

- **响应**: `data = null`
- **错误码**: 404-对方用户不存在, 400-不能给自己发消息

---

## 三、WebSocket协议

### 连接地址
- **URL**: `ws://host/ws/chat?token={jwt_token}`
- **认证**: 通过query参数token鉴权

### 消息格式
```json
{
  "type": "chat|read|ping|pong",
  "sessionId": 1,
  "content": "消息内容",
  "msgType": 1
}
```

### 消息类型

| type | 说明 | 方向 |
|------|------|------|
| chat | 聊天消息 | 双向 |
| read | 已读标记 | 客户端→服务端 |
| ping | 心跳请求 | 客户端→服务端 |
| pong | 心跳响应 | 服务端→客户端 |

### 消息内容类型

| msgType | 说明 |
|---------|------|
| 1 | 文字消息 |
| 2 | 图片消息 |

### 断线重连
- 重连间隔: 5秒
- 最大重试次数: 10次
- 心跳间隔: 30秒

---

## 四、VO定义

### NotificationVo
```java
public class NotificationVo {
    private Long id;
    private String type;
    private String title;
    private String content;
    private String relatedType;
    private Long relatedId;
    private Integer isRead;
    private LocalDateTime readTime;
    private LocalDateTime createTime;
}
```

### ChatSessionVo
```java
public class ChatSessionVo {
    private Long id;
    private Long otherUserId;
    private String otherNickname;
    private String otherAvatar;
    private String lastMessage;
    private LocalDateTime lastTime;
    private Integer unreadCount;
}
```

### ChatMessageVo
```java
public class ChatMessageVo {
    private Long id;
    private Long senderId;
    private String senderNickname;
    private String senderAvatar;
    private String content;
    private Integer msgType;   // 1-文字 2-图片
    private Integer isRead;
    private LocalDateTime createTime;
}
```

---

**文档版本**: v1.3.0
**最后更新**: 2026-06-28
