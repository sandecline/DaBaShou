import { createApp } from 'vue'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import zhCn from 'element-plus/dist/locale/zh-cn.mjs'

import App from './App.vue'
import router from './router'
import pinia from './stores'

import './styles/tailwind.css'
import './styles/global.scss'
import './styles/element.scss'

const app = createApp(App)

// 按需注册图标（只注册实际用到的）
import {
  ArrowLeft, Plus, Right, Check, Loading, Medal,
  Checked, Present, Timer, Expand, Fold,
  Search, User, Star, Warning, DataAnalysis,
  Wallet, Shop, Setting, Document, Edit, Delete, Refresh,
  HomeFilled, Menu, Collection, ChatDotRound,
} from '@element-plus/icons-vue'
const icons = {
  ArrowLeft, Plus, Right, Check, Loading, Medal,
  Checked, Present, Timer, Expand, Fold,
  Search, User, Star, Warning, DataAnalysis,
  Wallet, Shop, Setting, Document, Edit, Delete, Refresh,
  HomeFilled, Menu, Collection, ChatDotRound,
}
for (const [key, component] of Object.entries(icons)) {
  app.component(key, component)
}

app.use(ElementPlus, { locale: zhCn } as any)
app.use(router)
app.use(pinia)

app.mount('#app')

