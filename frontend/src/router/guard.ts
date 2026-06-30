import type { Router } from 'vue-router'
import { isLoggedIn } from '@/utils/auth'
import { useUserStore } from '@/stores/user'

export function setupRouterGuard(router: Router) {
  router.beforeEach(async (to, _from, next) => {
    document.title = `${(to.meta as any).title || '搭把手'} - 搭把手`

    const userStore = useUserStore()

    // 首次加载：等待自动登录完成
    if (!userStore.loginReady) {
      await userStore.autoLogin()
    }

    const requiresAuth = to.meta.requiresAuth as boolean | undefined

    // 需要登录但未登录 → 跳登录页
    if (requiresAuth && !isLoggedIn()) {
      next({ name: 'Login', query: { redirect: to.fullPath } })
      return
    }

    // 已登录用户访问登录/注册页 → 重定向首页
    if (isLoggedIn() && (to.path === '/login' || to.path === '/register')) {
      next({ name: 'Home' })
      return
    }

    next()
  })
}
