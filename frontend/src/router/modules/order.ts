import type { RouteRecordRaw } from 'vue-router'

const orderRoutes: RouteRecordRaw[] = [
  {
    path: '/order',
    name: 'OrderList',
    component: () => import('@/views/order/list.vue'),
    meta: { title: '我的订单', icon: 'Document', requiresAuth: true },
  },
  {
    path: '/order/:id',
    name: 'OrderDetail',
    component: () => import('@/views/order/detail.vue'),
    meta: { title: '订单详情', requiresAuth: true },
    props: true,
  },
  {
    path: '/order/:id/verify',
    name: 'OrderVerify',
    component: () => import('@/views/order/verify.vue'),
    meta: { title: '核销确认', requiresAuth: true },
    props: true,
  },
]

export default orderRoutes
