import type { RouteRecordRaw } from 'vue-router'

const demandRoutes: RouteRecordRaw[] = [
  {
    path: '/demand',
    name: 'DemandList',
    component: () => import('@/views/demand/list.vue'),
    meta: { title: '求助看板', icon: 'Collection' },
  },
  {
    path: '/demand/publish',
    name: 'DemandPublish',
    component: () => import('@/views/demand/publish.vue'),
    meta: { title: '发布求助', requiresAuth: true },
  },
  {
    path: '/demand/:id',
    name: 'DemandDetail',
    component: () => import('@/views/demand/detail.vue'),
    meta: { title: '需求详情' },
    props: true,
  },
]

export default demandRoutes
