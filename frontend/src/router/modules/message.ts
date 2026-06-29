import type { RouteRecordRaw } from 'vue-router'

const messageRoutes: RouteRecordRaw[] = [
  {
    path: '/message',
    name: 'MessageList',
    component: () => import('@/views/message/list.vue'),
    meta: { title: '消息中心', icon: 'ChatDotRound', requiresAuth: true },
  },
  {
    path: '/message/chat/:userId',
    name: 'Chat',
    component: () => import('@/views/message/chat.vue'),
    meta: { title: '聊天', requiresAuth: true },
    props: true,
  },
]

export default messageRoutes
