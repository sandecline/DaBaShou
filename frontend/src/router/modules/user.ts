import type { RouteRecordRaw } from 'vue-router'

const userRoutes: RouteRecordRaw[] = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/user/login.vue'),
    meta: { title: '登录', noLayout: true },
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('@/views/user/register.vue'),
    meta: { title: '注册', noLayout: true },
  },
  {
    path: '/user/profile',
    name: 'UserProfile',
    component: () => import('@/views/user/profile.vue'),
    meta: { title: '个人资料', icon: 'User', requiresAuth: true },
  },
  {
    path: '/user/shop',
    name: 'UserShop',
    component: () => import('@/views/user/shop.vue'),
    meta: { title: '我的小铺', icon: 'Shop', requiresAuth: true },
  },
  {
    path: '/user/shop/:userId',
    name: 'UserShopPreview',
    component: () => import('@/views/user/shop.vue'),
    meta: { title: '技能小铺' },
    props: true,
  },
  {
    path: '/user/points',
    name: 'UserPoints',
    component: () => import('@/views/user/points.vue'),
    meta: { title: '积分管理', icon: 'Wallet', requiresAuth: true },
  },
  {
    path: '/user/trust',
    name: 'UserTrust',
    component: () => import('@/views/user/trust.vue'),
    meta: { title: '信任分', icon: 'Medal', requiresAuth: true },
  },
]

export default userRoutes
