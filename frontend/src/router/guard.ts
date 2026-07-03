import type { Router } from 'vue-router'
import { isAdminSession, isLoggedIn } from '@/utils/auth'
import { ElMessage } from 'element-plus'

export function setupRouterGuard(router: Router) {
  router.beforeEach((to, _from, next) => {
    // 设置页面标题
    document.title = `${to.meta.title || '搭把手'} - 搭把手`

    const requiresAuth = to.meta.requiresAuth

    // 管理员访问前台首页 → 重定向到后台
    if (to.path === '/' && isLoggedIn() && isAdminSession()) {
      next('/admin')
      return
    }

    // 未登录用户访问需要认证的页面
    if (requiresAuth && !isLoggedIn()) {
      ElMessage.warning('请先登录')
      next({ name: 'Login', query: { redirect: to.fullPath } })
      return
    }

    // 非管理员访问管理后台
    if (to.meta.role === 'admin' && !isAdminSession()) {
      ElMessage.warning('无权访问管理后台')
      next({ name: 'Home' })
      return
    }

    // 已登录用户访问登录/注册页
    if (isLoggedIn() && (to.path === '/login' || to.path === '/register')) {
      if (isAdminSession()) {
        next('/admin')
      } else {
        next({ name: 'Home' })
      }
      return
    }

    next()
  })
}
