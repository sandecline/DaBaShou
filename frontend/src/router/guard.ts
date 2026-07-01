import type { Router } from 'vue-router'
import { isLoggedIn } from '@/utils/auth'
import { ElMessage } from 'element-plus'

const whiteList = ['/login', '/register', '/']

export function setupRouterGuard(router: Router) {
  router.beforeEach((to, _from, next) => {
    // 设置页面标题
    document.title = `${to.meta.title || '搭把手'} - 搭把手`

    const requiresAuth = to.meta.requiresAuth

    if (to.path === '/' && !isLoggedIn()) {
      next({ name: 'Login' })
      return
    }

    if (requiresAuth && !isLoggedIn()) {
      ElMessage.warning('请先登录')
      next({ name: 'Login', query: { redirect: to.fullPath } })
      return
    }

    // 已登录用户访问登录/注册页，重定向到首页
    if (isLoggedIn() && (to.path === '/login' || to.path === '/register')) {
      next({ name: 'Home' })
      return
    }

    next()
  })
}
