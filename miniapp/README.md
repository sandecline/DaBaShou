# 搭把手 — 微信小程序端

> 基于 uni-app 3.x (Vue3) 的校园技能共享互助平台小程序

## 技术栈

| 技术 | 版本 | 说明 |
|---|---|---|
| uni-app | 3.x | 跨端框架，一套代码跑微信小程序 + H5 |
| Vue | 3.4+ | 响应式 UI |
| Pinia | 2.x | 状态管理 |
| uView Plus | 3.x | UI 组件库（60+ 组件） |
| dayjs | 1.x | 时间格式化 |
| ESLint + Prettier | — | 代码规范 |

## 快速启动

### 前置要求

```bash
# 1. 安装依赖
cd miniapp
npm install

# 2. H5 模式（浏览器调试）
npm run dev:h5
# 访问 http://localhost:5174

# 3. 微信小程序模式
npm run dev
# 用微信开发者工具打开 dist/dev/mp-weixin
```

### 配套后端

小程序调用同一套后端 API，需先启动后端：

```bash
# 在 backend 目录
cd ../backend
mvn clean package -Dmaven.test.skip=true -T 1C
cd dabashou-api/target
java -jar dabashou-api-1.0.0-SNAPSHOT.jar --spring.profiles.active=dev --server.port=9090
```

或使用项目根目录的一键脚本：

```powershell
..\start-all.ps1 start
```

## 项目结构

```
miniapp/
├── src/
│   ├── pages/                 # 页面（下划线命名）
│   │   ├── index/             # 首页（技能货架浏览）
│   │   ├── shelf/             # 发布服务
│   │   ├── shelf-detail/      # 服务详情 + 下单
│   │   ├── demand/            # 需求看板
│   │   ├── demand-detail/     # 需求详情 + 接单
│   │   ├── order/             # 订单列表
│   │   ├── order-detail/      # 订单详情（支付/核销/确认/退款）
│   │   ├── chat/              # 聊天会话
│   │   ├── profile/           # 个人中心
│   │   ├── auth/              # 授权登录
│   │   └── about/             # 关于
│   ├── components/            # 公共组件
│   ├── services/              # API 接口层
│   │   ├── shelf.js           # 技能货架接口
│   │   ├── demand.js          # 需求接口
│   │   ├── order.js           # 订单接口
│   │   ├── user.js            # 用户/积分接口
│   │   ├── credit.js          # 信用评价接口
│   │   └── message.js         # 消息/通知接口
│   ├── utils/                 # 工具函数
│   │   ├── request.js         # 统一请求封装（Token/401/错误处理）
│   │   ├── auth.js            # 登录/Token 管理
│   │   └── format.js          # 时间/状态格式化
│   ├── store/                 # Pinia 状态管理
│   │   ├── user.js            # 用户状态
│   │   └── app.js             # 应用全局状态
│   ├── static/                # 静态资源
│   ├── App.vue                # 应用入口
│   ├── main.js                # 应用初始化
│   ├── pages.json             # 页面路由 + tabBar 配置
│   ├── manifest.json          # 应用配置
│   └── uni.scss               # 全局样式变量
├── .eslintrc.js
├── .prettierrc
├── package.json
└── vite.config.js
```

## 开发规范

### 页面开发

每个页面是一个目录，包含一个 `.vue` 单文件组件：

```vue
<template>
  <!-- WXML 模板 -->
</template>

<script setup>
// 组合式 API
import { ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
</script>

<style lang="scss" scoped>
/* 页面样式 */
</style>
```

### API 调用

统一通过 `services/` 下的文件调用：

```javascript
import { getShelfList } from '@/services/shelf'

const data = await getShelfList({ pageNum: 1, pageSize: 10 })
```

### 状态管理

```javascript
import { useUserStore } from '@/store/user'

const userStore = useUserStore()
if (userStore.isLogin) { /* ... */ }
```

## 环境配置

### 微信小程序

1. 在 `src/manifest.json` 中填入 `mp-weixin.appid`
2. **微信开发者工具** → 详情 → 本地设置 → 勾选"不校验合法域名、web-view（业务域名）"
3. 生产环境需在微信公众平台配置 `request 合法域名`

### H5 模式

vite.config.js 已配置 `/api` 代理到 `localhost:9090`，开发时直接访问即可。

## 页面与 API 对照

| 页面 | 路由 | 主要 API |
|---|---|---|
| 首页 | `/pages/index/index` | `shelf.js:getShelfList` |
| 服务详情 | `/pages/shelf-detail/shelf-detail` | `shelf.js:getShelfDetail` / `order.js:createOrderFromShelf` |
| 需求看板 | `/pages/demand/demand` | `demand.js:getDemandList` |
| 订单列表 | `/pages/order/order` | `order.js:getOrderList` |
| 个人中心 | `/pages/profile/profile` | `user.js:getProfile` |

## 需要后续完成的工作

- [ ] 在微信公众平台注册小程序，获取 AppID
- [ ] 后端实现 `POST /api/v1/auth/wechat-login` 接口
- [ ] 替换 tabBar 图标（`static/icons/` 下的占位图）
- [ ] 编写剩余页面（shelf/demand/order/chat 等）
- [ ] 后端部署到 HTTPS 服务器后更新 `src/utils/request.js` 中的 BASE_URL
