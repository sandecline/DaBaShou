import type { RouteRecordRaw } from 'vue-router'

const adminRoutes: RouteRecordRaw[] = [
  {
    path: '/admin',
    redirect: '/admin/users',
    meta: { title: '管理后台', icon: 'Setting', requiresAuth: true, role: 'admin' },
    children: [
      {
        path: 'users',
        name: 'AdminUsers',
        component: () => import('@/views/admin/user/index.vue'),
        meta: { title: '用户管理', requiresAuth: true, role: 'admin' },
      },
      {
        path: 'orders',
        name: 'AdminOrders',
        component: () => import('@/views/admin/order/index.vue'),
        meta: { title: '订单管理', requiresAuth: true, role: 'admin' },
      },
      {
        path: 'credit',
        name: 'AdminCredit',
        component: () => import('@/views/admin/credit/index.vue'),
        meta: { title: '信用管理', requiresAuth: true, role: 'admin' },
      },
      {
        path: 'system',
        name: 'AdminSystem',
        component: () => import('@/views/admin/system/index.vue'),
        meta: { title: '系统配置', requiresAuth: true, role: 'admin' },
      },
      {
        path: 'stat',
        name: 'AdminStat',
        component: () => import('@/views/admin/stat/index.vue'),
        meta: { title: '数据统计', requiresAuth: true, role: 'admin' },
      },
    ],
  },
]

export default adminRoutes
