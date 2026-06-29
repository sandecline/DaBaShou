import { createRouter, createWebHistory } from 'vue-router'
import type { RouteRecordRaw } from 'vue-router'
import { setupRouterGuard } from './guard'

import homeRoutes from './modules/home'
import skillRoutes from './modules/skill'
import demandRoutes from './modules/demand'
import orderRoutes from './modules/order'
import userRoutes from './modules/user'
import messageRoutes from './modules/message'
import adminRoutes from './modules/admin'

// 信用评价路由（单独定义，不在模块中）
const creditRoutes: RouteRecordRaw[] = [
  {
    path: '/credit',
    name: 'CreditList',
    component: () => import('@/views/credit/list.vue'),
    meta: { title: '评价中心', icon: 'Star', requiresAuth: true },
  },
  {
    path: '/credit/appeal',
    name: 'CreditAppeal',
    component: () => import('@/views/credit/appeal.vue'),
    meta: { title: '申诉中心', icon: 'Warning', requiresAuth: true },
  },
]

const statRoutes: RouteRecordRaw[] = [
  {
    path: '/stat',
    name: 'StatOverview',
    component: () => import('@/views/stat/overview.vue'),
    meta: { title: '数据统计', icon: 'DataAnalysis', requiresAuth: true },
  },
]

const routes: RouteRecordRaw[] = [
  ...homeRoutes,
  ...skillRoutes,
  ...demandRoutes,
  ...orderRoutes,
  ...userRoutes,
  ...messageRoutes,
  ...creditRoutes,
  ...statRoutes,
  ...adminRoutes,
  {
    path: '/:pathMatch(.*)*',
    name: 'NotFound',
    component: () => import('@/views/error/404.vue'),
    meta: { title: '页面不存在' },
  },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
  scrollBehavior() {
    return { top: 0 }
  },
})

setupRouterGuard(router)

export default router
