import type { RouteRecordRaw } from 'vue-router'

const adminRoutes: RouteRecordRaw[] = [
  {
    path: '/admin',
    name: 'AdminOverview',
    component: () => import('@/views/admin/overview.vue'),
    meta: { title: '管理后台', icon: 'Setting', requiresAuth: true, role: 'admin' },
  },
  {
    path: '/admin/users',
    name: 'AdminUsers',
    component: () => import('@/views/admin/user/index.vue'),
    meta: { title: '用户管理', requiresAuth: true, role: 'admin' },
  },
  {
    path: '/admin/orders',
    name: 'AdminOrders',
    component: () => import('@/views/admin/order/index.vue'),
    meta: { title: '订单管理', requiresAuth: true, role: 'admin' },
  },
  {
    path: '/admin/credit',
    name: 'AdminCredit',
    component: () => import('@/views/admin/credit/index.vue'),
    meta: { title: '信用管理', requiresAuth: true, role: 'admin' },
  },
  {
    path: '/admin/campus-auths',
    name: 'AdminCampusAuths',
    component: () => import('@/views/admin/campus/index.vue'),
    meta: { title: '校园认证', requiresAuth: true, role: 'admin' },
  },
  {
    path: '/admin/system',
    name: 'AdminSystem',
    component: () => import('@/views/admin/system/index.vue'),
    meta: { title: '系统配置', requiresAuth: true, role: 'admin' },
  },
  {
    path: '/admin/stat',
    name: 'AdminStat',
    component: () => import('@/views/admin/stat/index.vue'),
    meta: { title: '数据统计', requiresAuth: true, role: 'admin' },
  },
]

export default adminRoutes
