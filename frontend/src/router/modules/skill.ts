import type { RouteRecordRaw } from 'vue-router'

const skillRoutes: RouteRecordRaw[] = [
  {
    path: '/skill',
    name: 'SkillList',
    component: () => import('@/views/skill/list.vue'),
    meta: { title: '技能广场', icon: 'Menu' },
  },
  {
    path: '/skill/publish',
    name: 'SkillPublish',
    component: () => import('@/views/skill/publish.vue'),
    meta: { title: '发布技能', requiresAuth: true },
  },
  {
    path: '/skill/:id',
    name: 'SkillDetail',
    component: () => import('@/views/skill/detail.vue'),
    meta: { title: '技能详情' },
    props: true,
  },
]

export default skillRoutes
