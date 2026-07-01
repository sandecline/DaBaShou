# 搭把手 — 微信小程序项目说明与开发计划书

> **文档版本**：v1.0.0  
> **创建日期**：2026年7月1日  
> **项目性质**：学习展示项目（不上线生产环境）  
> **关联文档**：[需求清单](./requirements.md) | [API文档](./api/) | [协作约束](../AGENTS.md) | [主项目README](../README.md)

---

## 一、项目概述

### 1.1 项目定位

**搭把手**是一个校园共享技能互助平台的微信小程序端。与现有 Vue 3 Web 端并行，小程序作为微信生态内的轻量级入口，复用同一套后端 API。

### 1.2 与现有项目的关系

```
                     ┌─────────────────────────┐
                     │   Spring Boot 后端集群    │
                     │   (12 个微服务模块)        │
                     └───────────┬─────────────┘
                                 │ REST API (HTTPS)
                 ┌───────────────┼───────────────┐
                 ▼               ▼               ▼
        ┌────────────┐  ┌────────────┐  ┌────────────┐
        │  Vue 3 Web  │  │ 微信小程序   │  │  管理后台    │
        │  (已有)      │  │  (本项目)    │  │  (已有)      │
        └────────────┘  └────────────┘  └────────────┘
```

- **小程序** = 新的前端载体（原生框架 + TDesign 组件库）
- **后端** = 完全复用现有 Spring Boot 微服务集群
- **数据库** = 共享同一套 MySQL 表结构

### 1.3 目标用户

以在校大学生为主，通过微信扫码或群分享进入小程序，完成技能浏览 → 发布/接单 → 即时沟通 → 积分结算的完整互助闭环。

---

## 二、技术架构决策

以下 10 项决策经过逐项讨论，已全部确认：

| # | 决策项 | 最终选择 | 关键考量 |
|---|--------|---------|---------|
| 1 | 开发框架 | **原生开发** (WXML+WXSS+TS) | 微信能力最深度集成，性能最优，API第一时间可用 |
| 2 | 开发语言 | **TypeScript** | 微信原生API有官方类型定义(`@types/wechat-miniprogram`)，类型安全减少低级bug |
| 3 | 渲染引擎 | **WebView** | 兼容性最广，CSS编写与Web习惯一致，社区资料丰富 |
| 4 | UI组件库 | **TDesign Miniprogram** (腾讯官方) | 60+组件，与Web端Element Plus设计语言一致，TypeScript支持完整 |
| 5 | 登录方式 | **微信静默登录** + 关键时刻绑手机号 | `wx.login()`无感登录，发布技能/接单时触发手机号授权 |
| 6 | 积分支付 | **纯虚拟积分** + PaymentService接口预留 | 无需微信支付商户号，预留接口方便未来升级 |
| 7 | 底部导航 | **四Tab**：首页 / 发布 / 消息 / 我的 | 市场验证最成熟的布局，发布放中间促转化 |
| 8 | 分包策略 | **方案A+**（主包+一个user分包） | 主包950KB左右，分包250KB；覆盖全部9个业务模块 |
| 9 | 即时通讯 | **WebSocket自建** | 后端已有`dabashou-message`模块，数据完全自控 |
| 10 | 订阅消息 | **跳过**（演示项目不需要） | 前端代码中预留订阅消息调用入口，后续可快速开启 |

---

## 三、技术架构详图

### 3.1 整体技术栈

```
┌──────────────────────────────────────────────────────────┐
│                   搭把手 微信小程序                         │
│                                                          │
│  框架层：原生 WXML + WXSS + TypeScript                     │
│  组件库：TDesign Miniprogram v1.x                        │
│  网络层：wx.request (HTTPS) + wx.connectSocket (WSS)     │
│  存储层：wx.getStorageSync / wx.setStorageSync           │
│  状态层：app.globalData + Page.data + Component.data      │
│                                                          │
│  ┌─────────┐  ┌─────────┐  ┌─────────┐  ┌─────────┐    │
│  │ 首页     │  │ 发布     │  │ 消息     │  │ 我的     │    │
│  │ (技能浏览 │  │ (技能/   │  │ (IM聊天  │  │ (个人中心 │    │
│  │  需求看板)│  │  需求)   │  │  通知)   │  │  管理)    │    │
│  └─────────┘  └─────────┘  └─────────┘  └─────────┘    │
└──────────────────────────────────────────────────────────┘
                           │
              HTTPS / WSS (复用现有后端)
                           │
┌──────────────────────────────────────────────────────────┐
│              Spring Boot 后端 (已建)                       │
│  dabashou-api (网关) → 分发到各业务模块                     │
└──────────────────────────────────────────────────────────┘
```

### 3.2 网络通信架构

```
┌──────────────┐                    ┌─────────────────────┐
│  小程序前端   │  wx.request()      │  dabashou-api:8080  │
│              │◄──────────────────►│  (Nginx 反向代理)    │
│  utils/      │  JSON + Bearer     │                     │
│  request.ts  │  Token             │  /api/v1/skills/*   │
│              │                    │  /api/v1/orders/*   │
│              │  wx.connectSocket()│  /api/v1/demands/*  │
│  services/   │◄──────────────────►│  ...                │
│  websocket.ts│  WSS (消息推送)     │                     │
│              │                    │  /ws/chat           │
└──────────────┘                    └─────────────────────┘
```

### 3.3 登录认证流程

```
用户打开小程序
    │
    ▼
wx.login() → 获取临时 code
    │
    ▼
POST /api/v1/auth/wechat-login { code }
    │
    ▼
后端换 openid → 生成 JWT token → 返回 { access_token, refresh_token }
    │
    ▼
小程序存储 token 到 Storage
    │
    ├── 浏览首页/看板 → 无需额外认证
    │
    └── 首次发布/接单 → 触发手机号绑定
            │
            ▼
        button open-type="getPhoneNumber"
            │
            ▼
        后端解密手机号 → 关联校园身份 → 更新用户信息
```

---

## 四、项目目录结构

### 4.1 完整目录树

```
miniprogram/
├── app.ts                       # App 入口：生命周期、全局数据
├── app.json                     # 全局配置：pages、window、tabBar
├── app.wxss                     # 全局样式：CSS 变量、基础重置
├── project.config.json          # 微信开发者工具配置
├── tsconfig.json                # TypeScript 配置
├── package.json                 # npm 依赖 (TDesign)
│
├── pages/                       # ===== 主包页面 =====
│   ├── index/                   # 首页 Tab — 技能分类 + 求助看板
│   │   ├── index.ts
│   │   ├── index.json
│   │   ├── index.wxml
│   │   └── index.wxss
│   ├── publish/                 # 发布 Tab — 发布入口（选技能/求助）
│   │   ├── publish.ts
│   │   ├── publish.json
│   │   ├── publish.wxml
│   │   └── publish.wxss
│   ├── message/                 # 消息 Tab — 聊天列表
│   │   ├── message.ts
│   │   ├── message.json
│   │   ├── message.wxml
│   │   └── message.wxss
│   ├── mine/                    # 我的 Tab — 个人主页入口
│   │   ├── mine.ts
│   │   ├── mine.json
│   │   ├── mine.wxml
│   │   └── mine.wxss
│   ├── skill-detail/            # 技能详情页
│   ├── demand-detail/           # 需求详情页
│   └── chat/                    # 聊天详情页
│
├── subpackages/                 # ===== 分包 =====
│   └── user/                    # 用户分包
│       ├── order-list/          # 订单列表
│       ├── order-detail/        # 订单详情 + 核销
│       ├── shelf-manage/        # 我的小铺（技能货架管理）
│       ├── point-detail/        # 积分明细 + 担保池
│       ├── credit/              # 信用评价
│       ├── my-stats/            # 个人数据统计
│       ├── profile-edit/        # 编辑资料
│       ├── settings/            # 设置
│       └── about/               # 关于
│
├── components/                  # ===== 公共组件 =====
│   ├── skill-card/              # 技能卡片（列表+详情复用）
│   ├── demand-card/             # 需求卡片
│   ├── user-avatar/             # 用户头像+信任分徽章
│   ├── point-badge/             # 积分展示组件
│   ├── empty-state/             # 空状态占位
│   └── loading-skeleton/        # 骨架屏加载
│
├── utils/                       # ===== 工具函数 =====
│   ├── request.ts               # 统一网络请求封装（wx.request Promisify）
│   ├── auth.ts                  # 登录认证 + Token管理
│   ├── websocket.ts             # WebSocket 连接管理
│   ├── validator.ts             # 表单验证工具
│   ├── date.ts                  # 日期格式化（dayjs 封装）
│   └── storage.ts               # Storage 读写封装
│
├── services/                    # ===== 业务服务层 =====
│   ├── skill.ts                 # 技能相关 API
│   ├── demand.ts                # 需求相关 API
│   ├── order.ts                 # 订单相关 API
│   ├── point.ts                 # 积分相关 API
│   ├── message.ts               # 消息相关 API
│   ├── credit.ts                # 信用评价 API
│   ├── user.ts                  # 用户信息 API
│   └── payment.ts               # PaymentService 接口定义（预留）
│
├── types/                       # ===== TypeScript 类型 =====
│   ├── api-response.d.ts        # 统一响应格式
│   ├── user.d.ts                # 用户类型
│   ├── skill.d.ts               # 技能类型
│   ├── demand.d.ts              # 需求类型
│   ├── order.d.ts               # 订单类型（含状态机枚举）
│   ├── point.d.ts               # 积分类型
│   ├── message.d.ts             # 消息类型
│   ├── credit.d.ts              # 信用评价类型
│   └── global.d.ts              # 全局类型声明
│
└── miniprogram_npm/             # npm 构建产物（TDesign 组件）
    └── tdesign-miniprogram/     # 自动生成，不手动编辑
```

### 4.2 分包配置 (app.json)

```json
{
  "pages": [
    "pages/index/index",
    "pages/publish/publish",
    "pages/message/message",
    "pages/mine/mine",
    "pages/skill-detail/skill-detail",
    "pages/demand-detail/demand-detail",
    "pages/chat/chat"
  ],
  "subPackages": [
    {
      "root": "subpackages/user",
      "pages": [
        "order-list/order-list",
        "order-detail/order-detail",
        "shelf-manage/shelf-manage",
        "point-detail/point-detail",
        "credit/credit",
        "my-stats/my-stats",
        "profile-edit/profile-edit",
        "settings/settings",
        "about/about"
      ]
    }
  ],
  "preloadRule": {
    "pages/mine/mine": {
      "packages": ["subpackages/user"],
      "network": "all"
    }
  }
}
```

> **预加载策略**：用户点击"我的"Tab 时，后台自动下载 user 分包。因为在"我的"页面下，订单、积分、设置等都是高频跳转目标。

---

## 五、页面路由与导航设计

### 5.1 页面跳转关系图

```
                          ┌─────────────────┐
                          │     启动页       │
                          │  (wx.login静默)  │
                          └────────┬────────┘
                                   │
              ┌────────────────────┼────────────────────┐
              ▼                    ▼                    ▼
      ┌──────────┐        ┌──────────┐        ┌──────────┐
      │ 首页 Tab  │        │ 发布 Tab  │        │ 我的 Tab  │
      └────┬─────┘        └────┬─────┘        └────┬─────┘
           │                   │                   │
    ┌──────┼──────┐      ┌─────┼─────┐      ┌──────┼──────┐
    ▼      ▼      ▼      ▼     ▼     ▼      ▼      ▼      ▼
  技能   需求   搜索   发布   发布   (略)   订单   积分   设置
  详情   详情          技能   求助        列表   明细   
           │                   │           │      │
           ▼                   ▼           ▼      ▼
        ┌──────┐          ┌──────┐    订单详情  信用评价
        │ 聊天  │          │ 聊天  │    +核销
        │ 详情  │          │ 详情  │
        └──────┘          └──────┘
```

### 5.2 页面路由表

| 路径 | 页面 | 参数 | 导航方式 | 所属 |
|------|------|------|---------|------|
| `pages/index/index` | 首页 | - | Tab | 主包 |
| `pages/publish/publish` | 发布入口 | - | Tab | 主包 |
| `pages/message/message` | 消息列表 | - | Tab | 主包 |
| `pages/mine/mine` | 我的 | - | Tab | 主包 |
| `pages/skill-detail/skill-detail` | 技能详情 | `?id=xxx` | `navigateTo` | 主包 |
| `pages/demand-detail/demand-detail` | 需求详情 | `?id=xxx` | `navigateTo` | 主包 |
| `pages/chat/chat` | 聊天详情 | `?targetId=xxx` | `navigateTo` | 主包 |
| `subpackages/user/order-list/order-list` | 订单列表 | `?tab=all\|pending\|doing` | `navigateTo` | user分包 |
| `subpackages/user/order-detail/order-detail` | 订单详情+核销 | `?id=xxx` | `navigateTo` | user分包 |
| `subpackages/user/shelf-manage/shelf-manage` | 技能货架管理 | - | `navigateTo` | user分包 |
| `subpackages/user/point-detail/point-detail` | 积分明细 | - | `navigateTo` | user分包 |
| `subpackages/user/credit/credit` | 信用评价 | - | `navigateTo` | user分包 |
| `subpackages/user/my-stats/my-stats` | 数据统计 | - | `navigateTo` | user分包 |
| `subpackages/user/profile-edit/profile-edit` | 编辑资料 | - | `navigateTo` | user分包 |
| `subpackages/user/settings/settings` | 设置 | - | `navigateTo` | user分包 |
| `subpackages/user/about/about` | 关于 | - | `navigateTo` | user分包 |

### 5.3 跨分包跳转注意

- 主包 → 分包：直接 `wx.navigateTo({ url: '/subpackages/user/...' })` ✅
- 分包 → 主包：直接 `wx.navigateTo({ url: '/pages/...' })` ✅  
- 分包 → 另一分包：❌ 不允许直接跳，需先 `wx.switchTab` 回主包再跳

---

## 六、组件设计

### 6.1 TDesign 组件使用规划

| TDesign 组件 | 使用场景 |
|-------------|---------|
| `t-tabs` | 首页的技能/需求Tab切换 |
| `t-search` | 技能搜索、需求搜索 |
| `t-cell` / `t-cell-group` | 个人中心设置列表、技能详情信息行 |
| `t-tag` | 技能分类标签、紧急程度标签、订单状态标签 |
| `t-button` | 发布按钮、接单按钮、核销确认按钮 |
| `t-dialog` | 发布确认、取消确认、评价弹窗 |
| `t-toast` | 操作成功/失败提示 |
| `t-image` | 技能封面图、用户头像 |
| `t-input` / `t-textarea` | 表单输入 |
| `t-picker` | 技能分类选择、时间选择 |
| `t-upload` | 技能图片上传 |
| `t-rate` | 星级评分（信用评价） |
| `t-empty` | 空列表占位 |
| `t-skeleton` | 列表骨架屏加载 |
| `t-badge` | 未读消息数 |
| `t-navbar` | 页面导航栏（自定义时） |
| `t-icon` | 各类图标 |
| `t-loading` | 加载中状态 |
| `t-swipe-cell` | 消息列表滑动删除 |
| `t-dropdown-menu` | 首页筛选（分类、排序） |

### 6.2 自定义组件清单

| 组件 | 文件路径 | Props | Events | 复用场景 |
|------|---------|-------|--------|---------|
| `skill-card` | `components/skill-card/` | `skill: Skill`, `showDistance: boolean` | `bind:tap` | 首页列表、搜索结果、我的小铺 |
| `demand-card` | `components/demand-card/` | `demand: Demand` | `bind:tap` | 首页看板、搜索结果 |
| `user-avatar` | `components/user-avatar/` | `user: UserBrief`, `size: 'sm'\|'md'\|'lg'` | - | 全局复用（头像+信任分徽章） |
| `point-badge` | `components/point-badge/` | `amount: number`, `showIcon: boolean` | - | 积分展示 |
| `empty-state` | `components/empty-state/` | `text: string`, `icon: string` | `bind:action` | 空列表占位 |
| `loading-skeleton` | `components/loading-skeleton/` | `type: 'card'\|'list'\|'detail'` | - | 首次加载骨架屏 |

---

## 七、核心数据流设计

### 7.1 网络请求封装 (utils/request.ts)

```typescript
// 统一请求封装：自动带Token、401自动刷新、统一错误处理

interface RequestOptions {
  url: string;
  method?: 'GET' | 'POST' | 'PUT' | 'DELETE';
  data?: any;
  header?: Record<string, string>;
  showLoading?: boolean;
}

interface ApiResponse<T = any> {
  code: number;
  msg: string;
  data: T;
}

function request<T = any>(options: RequestOptions): Promise<ApiResponse<T>> {
  return new Promise((resolve, reject) => {
    const token = wx.getStorageSync('access_token');
    
    wx.request({
      url: `${BASE_URL}${options.url}`,
      method: options.method || 'GET',
      data: options.data,
      header: {
        'Content-Type': 'application/json',
        ...(token ? { Authorization: `Bearer ${token}` } : {}),
        ...options.header,
      },
      success(res) {
        if (res.statusCode === 401) {
          return refreshToken().then(() => request(options).then(resolve).catch(reject));
        }
        if (res.statusCode === 200) {
          resolve(res.data as ApiResponse<T>);
        } else {
          reject({ code: res.statusCode, msg: (res.data as any)?.msg || '请求失败' });
        }
      },
      fail(err) {
        reject({ code: -1, msg: '网络错误', detail: err });
      },
    });
  });
}
```

### 7.2 状态管理模式

原生小程序没有 Vuex/Pinia，采用**分层状态管理**：

| 层级 | 机制 | 适用场景 |
|------|------|---------|
| 全局状态 | `app.globalData` | 用户信息、未读消息数、token |
| 页面状态 | `Page.data` + `setData` | 页面数据、表单数据、加载状态 |
| 组件状态 | `Component.data` + `properties` | 组件内部状态 |
| 跨页面通信 | `wx.eventBus` (getApp().eventBus) 或 Storage | 登录状态变更、消息已读同步 |
| 缓存 | `wx.getStorageSync/setStorageSync` | token、用户偏好、草稿暂存 |

### 7.3 业务服务层示例 (services/order.ts)

```typescript
import { request } from '../utils/request';
import type { Order, OrderDetail, CreateOrderParams } from '../types/order';

// 订单服务
export const orderService = {
  // 获取订单列表
  getList(params: { pageNum: number; pageSize: number; status?: number }) {
    return request<{ total: number; list: Order[] }>({
      url: '/api/v1/orders',
      data: params,
    });
  },

  // 获取订单详情（含核销码）
  getDetail(orderId: number) {
    return request<OrderDetail>({
      url: `/api/v1/orders/${orderId}`,
    });
  },

  // 创建订单（从技能详情页下单）
  createOrder(params: CreateOrderParams) {
    return request<{ orderId: number }>({
      url: '/api/v1/orders',
      method: 'POST',
      data: params,
    });
  },

  // 确认完成（买家确认服务完成）
  confirmComplete(orderId: number) {
    return request<void>({
      url: `/api/v1/orders/${orderId}/complete`,
      method: 'PUT',
    });
  },

  // 取消订单
  cancel(orderId: number) {
    return request<void>({
      url: `/api/v1/orders/${orderId}/cancel`,
      method: 'PUT',
    });
  },
};
```

---

## 八、后端模块映射

### 8.1 小程序页面 ↔ 后端模块对照表

| 小程序页面 | 后端模块 | 主要 API |
|-----------|---------|---------|
| 首页（技能浏览） | `dabashou-skill` | `GET /api/v1/skills` 列表/搜索/分类 |
| 首页（需求看板） | `dabashou-demand` | `GET /api/v1/demands` 列表/搜索 |
| 技能详情 | `dabashou-skill` | `GET /api/v1/skills/:id` |
| 需求详情 | `dabashou-demand` | `GET /api/v1/demands/:id` |
| 发布技能 | `dabashou-skill` | `POST /api/v1/skills` |
| 发布需求 | `dabashou-demand` | `POST /api/v1/demands` |
| 消息列表 | `dabashou-message` | `GET /api/v1/messages/conversations` |
| 聊天详情 | `dabashou-message` | WebSocket 实时通信 |
| 我的（入口） | `dabashou-user` | `GET /api/v1/users/me` |
| 订单列表/详情 | `dabashou-order` | `GET /api/v1/orders` |
| 核销 | `dabashou-order` | `POST /api/v1/orders/:id/verify` |
| 积分明细 | `dabashou-point` | `GET /api/v1/points/transactions` |
| 信用评价 | `dabashou-credit` | `GET/POST /api/v1/reviews` |
| 技能货架管理 | `dabashou-shelf` | `GET /api/v1/shelf` + `PUT /api/v1/shelf/:id` |
| 数据统计 | `dabashou-stat` | `GET /api/v1/stats/mine` |
| 个人资料编辑 | `dabashou-user` | `PUT /api/v1/users/me` |
| ❌ 系统管理 | `dabashou-system` | 小程序不需要（Web管理后台） |
| ❌ 管理后台 | `dabashou-admin` | 小程序不需要（Web管理后台） |
| ❌ 公共模块 | `dabashou-common` | 后端纯工具模块，无前端页面 |

> 12 个后端模块中 **9 个需要小程序页面**，**3 个不需要**。

### 8.2 PaymentService 接口预留设计

```typescript
// services/payment.ts — 当前为虚拟积分实现，预留微信支付接口

export interface IPaymentService {
  /** 冻结积分（创建订单时调用） */
  freezePoints(userId: number, amount: number, orderId: number): Promise<void>;
  /** 解冻并结算积分（订单完成时调用） */
  settlePoints(orderId: number): Promise<void>;
  /** 退还积分（订单取消/超时时调用） */
  refundPoints(orderId: number, penaltyRate?: number): Promise<void>;
}

// 当前实现：虚拟积分
export class VirtualPaymentService implements IPaymentService {
  async freezePoints(userId: number, amount: number, orderId: number) {
    return request({ url: '/api/v1/points/freeze', method: 'POST', data: { userId, amount, orderId } });
  }
  async settlePoints(orderId: number) {
    return request({ url: `/api/v1/points/settle/${orderId}`, method: 'PUT' });
  }
  async refundPoints(orderId: number, penaltyRate?: number) {
    return request({ url: `/api/v1/points/refund/${orderId}`, method: 'PUT', data: { penaltyRate } });
  }
}

// 未来扩展：微信支付
// export class WechatPaymentService implements IPaymentService { ... }

// 统一导出
export const paymentService: IPaymentService = new VirtualPaymentService();
```

---

## 九、开发计划

### 9.1 开发阶段划分

| 阶段 | 内容 | 产出物 | 依赖 |
|------|------|-------|------|
| **Phase 0：环境搭建** | 微信开发者工具配置、TDesign安装、TS配置、项目骨架 | 可运行的空壳小程序 | 微信AppID |
| **Phase 1：基础设施** | `utils/` 全部文件（request、auth、websocket、storage）、`types/` 类型定义、`app.ts/json/wxss` 全局配置 | 网络层+类型层完整 | Phase 0 |
| **Phase 2：首页 + 发布** | 首页Tab（技能浏览+需求看板）、技能详情、需求详情、发布入口+表单 | 核心浏览+发布闭环 | Phase 1 |
| **Phase 3：消息 + 聊天** | 消息列表Tab、聊天详情页、WebSocket 实时通信 | IM 基础功能 | Phase 1 |
| **Phase 4：我的 + 分包** | 我的Tab入口、订单列表/详情、积分明细、技能货架管理、信用评价 | 个人中心全功能 | Phase 2 |
| **Phase 5：完善 + 联调** | 个人资料编辑、设置、数据统计、与后端API联调、Mock数据替换 | 完整可演示的小程序 | Phase 4 |

### 9.2 各阶段详细任务

#### Phase 0：环境搭建（预估文件数：6）

- [x] 架构决策确认（已完成 10 项决策）
- [ ] 申请微信测试号 AppID（或使用现有测试号）
- [ ] 安装微信开发者工具
- [ ] 创建小程序项目（原生 + TypeScript 模板）
- [ ] `npm init` + `npm i tdesign-miniprogram`
- [ ] 配置 `project.config.json`（启用 TypeScript、npm 构建）
- [ ] 创建 `tsconfig.json`
- [ ] 在微信开发者工具中构建 npm

#### Phase 1：基础设施（预估文件数：15）

- [ ] `types/api-response.d.ts`：统一响应格式类型
- [ ] `types/user.d.ts`：用户类型定义
- [ ] `types/skill.d.ts`：技能类型定义
- [ ] `types/demand.d.ts`：需求类型定义
- [ ] `types/order.d.ts`：订单类型 + 状态机枚举
- [ ] `types/point.d.ts`：积分类型定义
- [ ] `types/message.d.ts`：消息类型定义
- [ ] `types/credit.d.ts`：信用评价类型定义
- [ ] `utils/request.ts`：网络请求封装（Promisify wx.request）
- [ ] `utils/auth.ts`：wx.login + Token 管理 + 手机号绑定
- [ ] `utils/websocket.ts`：WebSocket 连接管理（心跳+重连）
- [ ] `utils/storage.ts`：Storage 读写封装
- [ ] `utils/validator.ts`：表单验证（手机号、积分范围等）
- [ ] `utils/date.ts`：日期工具（dayjs）
- [ ] `app.ts` + `app.json` + `app.wxss`：全局配置

#### Phase 2：首页 + 发布（预估文件数：20）

- [ ] `components/skill-card/`：技能卡片组件
- [ ] `components/demand-card/`：需求卡片组件
- [ ] `components/loading-skeleton/`：骨架屏组件
- [ ] `components/empty-state/`：空状态组件
- [ ] `pages/index/`：首页（技能列表 + 需求看板 + 搜索）
- [ ] `pages/skill-detail/`：技能详情（展示+下单入口）
- [ ] `pages/demand-detail/`：需求详情（展示+揭榜入口）
- [ ] `pages/publish/`：发布入口 + 技能表单 + 需求表单
- [ ] `services/skill.ts`：技能 API 封装
- [ ] `services/demand.ts`：需求 API 封装

#### Phase 3：消息 + 聊天（预估文件数：10）

- [ ] `pages/message/`：消息列表 Tab
- [ ] `pages/chat/`：聊天详情页
- [ ] `services/message.ts`：消息 API 封装
- [ ] WebSocket 消息类型处理（文本、图片、系统消息）

#### Phase 4：我的 + 分包（预估文件数：25）

- [ ] `components/user-avatar/`：头像+信任分组件
- [ ] `components/point-badge/`：积分展示组件
- [ ] `pages/mine/`：我的 Tab 入口
- [ ] `subpackages/user/order-list/`：订单列表
- [ ] `subpackages/user/order-detail/`：订单详情 + 核销页
- [ ] `subpackages/user/shelf-manage/`：技能货架管理
- [ ] `subpackages/user/point-detail/`：积分明细
- [ ] `subpackages/user/credit/`：信用评价
- [ ] `services/order.ts`：订单 API 封装
- [ ] `services/point.ts`：积分 API 封装
- [ ] `services/credit.ts`：信用评价 API 封装
- [ ] `services/payment.ts`：PaymentService 接口+虚拟实现

#### Phase 5：完善 + 联调（预估文件数：10）

- [ ] `subpackages/user/profile-edit/`：编辑资料
- [ ] `subpackages/user/settings/`：设置页
- [ ] `subpackages/user/my-stats/`：数据统计
- [ ] `subpackages/user/about/`：关于页
- [ ] 与后端 API 联调，替换 Mock 数据
- [ ] 全局错误处理、网络异常降级
- [ ] 分享配置（`onShareAppMessage`）
- [ ] 最终检查：TDesign 组件引用、TS 编译无错误、开发者工具预览正常

**预估文件总数：约 90-100 个文件**

---

## 十、风险与限制

### 10.1 已知限制

| 限制项 | 影响 | 应对 |
|--------|------|------|
| 无已注册微信 AppID | 部分 API（手机号获取、订阅消息）无法完整测试 | 优先使用 Mock 数据；核心流程用开发者工具模拟 |
| 后端服务可能未启动 | 小程序请求全部失败 | `utils/request.ts` 内置 Mock 降级模式 |
| 原生框架学习曲线 | 团队需学习 WXML/WXSS 语法 | 参考本计划书中的代码模板和示例 |
| TDesign 包体积 | npm 构建后占 ~200KB | 主包预估 950KB，2MB 限制内有余量 |
| WebSocket 后端待开发 | 聊天功能可能需 Mock | 先用 HTTP 轮询模拟，后续切换 WS |

### 10.2 技术债务标记

- `// TODO: 替换为真实 API` — Mock 数据处统一标记
- `// TODO: PaymentService 切换微信支付` — 积分服务接口处标记
- `// TODO: 订阅消息接入` — 关键节点预留 `wx.requestSubscribeMessage` 调用点

---

## 十一、附录

### A. 关键技术参考

| 主题 | 文档链接 |
|------|---------|
| 微信小程序框架 | https://developers.weixin.qq.com/miniprogram/dev/framework/ |
| TDesign Miniprogram | https://tdesign.tencent.com/miniprogram/overview |
| 微信小程序 API 类型 | `@types/wechat-miniprogram` (npm) |
| WebSocket API | https://developers.weixin.qq.com/miniprogram/dev/api/network/websocket/wx.connectSocket.html |
| 登录流程 | https://developers.weixin.qq.com/miniprogram/dev/framework/open-ability/login.html |

### B. 已确认决策记录

| 日期 | 决策项 | 结论 |
|------|--------|------|
| 2026-07-01 | 开发框架 | 原生开发 |
| 2026-07-01 | 语言 | TypeScript |
| 2026-07-01 | 渲染引擎 | WebView |
| 2026-07-01 | UI 组件库 | TDesign Miniprogram |
| 2026-07-01 | 登录方式 | 微信静默登录 + 关键时刻绑手机号 |
| 2026-07-01 | 积分支付 | 纯虚拟积分 + PaymentService接口预留 |
| 2026-07-01 | 底部导航 | 四Tab（首页/发布/消息/我的） |
| 2026-07-01 | 分包策略 | 方案A+（主包+user分包，覆盖9个模块） |
| 2026-07-01 | 即时通讯 | WebSocket 自建 |
| 2026-07-01 | 订阅消息 | 跳过（演示项目） |

---

**文档版本**：v1.0.0  
**最后更新**：2026年7月1日  
**下次评审**：Phase 0 完成后更新
