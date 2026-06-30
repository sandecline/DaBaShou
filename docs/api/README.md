# 搭把手 API文档索引

## 全局规范
- [全局API契约规范](./global.md) - 统一响应格式、错误码、分页、认证、幂等、命名规范

## 模块API文档

| 模块 | 文档 | 前缀 | 端点数 | 说明 |
|------|------|------|--------|------|
| 用户与认证 | [auth-user.md](./auth-user.md) | `/api/v1/auth`, `/api/v1/user` | 13 | 注册、登录、资料、校园认证、信任分 |
| 技能 | [skill.md](./skill.md) | `/api/v1/skills` | 7 | 分类树、标签、用户技能CRUD |
| 技能货架 | [shelf.md](./shelf.md) | `/api/v1/shelves` | 12 | 服务发布、搜索、上下架、时间格子 |
| 需求 | [demand.md](./demand.md) | `/api/v1/demands` | 11 | 需求发布、看板、揭榜、智能匹配 |
| 订单 | [order.md](./order.md) | `/api/v1/orders` | 15 | 创建、支付、核销、争议、仲裁（状态机核心） |
| 积分 | [point.md](./point.md) | `/api/v1/points` | 8 | 余额、流水、签到、担保池 |
| 消息 | [message.md](./message.md) | `/api/v1/messages`, `/api/v1/chat` | 10 | 通知、WebSocket聊天、会话 |
| 信用评价 | [credit.md](./credit.md) | `/api/v1/credits`, `/api/v1/reviews`, `/api/v1/violations`, `/api/v1/appeals` | 9 | 评价、举报、申诉 |
| 数据统计 | [stat.md](./stat.md) | `/api/v1/stats`, `/api/v1/admin/stats` | 9 | 个人/平台概览、趋势、热度 |
| 系统管理 | [system.md](./system.md) | `/api/v1/files`, `/api/v1/admin` | 12 | 文件、角色、权限、日志、配置 |
| 管理后台 | [admin.md](./admin.md) | `/api/v1/admin` | 15 | 用户、订单、信用、认证审核 |

## 前后端接口文件分工

- 后端接口实现位于各业务模块的`controller`包，DTO/VO位于对应模块的`dto`/`vo`包。
- 后端接口契约说明统一维护在本目录，按模块拆分，不在聊天记录或临时文档中作为唯一来源。
- 前端接口调用统一维护在`frontend/src/api/`，类型维护在`frontend/src/types/api.ts`或模块化类型文件。
- 修改接口路径、方法、参数、响应字段、错误码或鉴权规则时，必须同步更新后端Controller/DTO/VO、本目录文档、前端API调用文件和前端类型定义。

## 状态机

订单模块使用8状态硬流转，详见 [order.md](./order.md) 一、订单状态机。

## 认证与权限

- 公开接口: 注册、登录、验证码、文件下载
- 用户接口: `/api/v1/**` 需JWT
- 管理接口: `/api/admin/v1/**` 需管理员角色
- 详见 [global.md](./global.md) 四、认证机制

## 幂等接口

创建订单、支付、核销、仲裁需要 `X-Idempotent-Token` 请求头。
详见 [global.md](./global.md) 五、幂等Token。

---

**文档版本**: v1.1.0
**最后更新**: 2026-06-30
