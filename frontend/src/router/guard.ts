import type { Router } from 'vue-router'
import { isLoggedIn } from '@/utils/auth'
import { useUserStore } from '@/stores/user'

let loginPromise: Promise<boolean> | null = null

export function setupRouterGuard(router: Router) {
  router.beforeEach((to, _from, next) => {
    document.title = `${(to.meta as any).title || '搭把手'} - 搭把手`

    const userStore = useUserStore()
    const requiresAuth = to.meta.requiresAuth as boolean | undefined

    // 已登录用户访问登录/注册页 → 重定向首页
    if (isLoggedIn() && (to.path === '/login' || to.path === '/register')) {
      next({ name: 'Home' })
      return
    }

    // 如果已登录，直接放行
    if (isLoggedIn()) {
      next()
      return
    }

    // 未登录但不需要认证的页面，直接放行，后台触发自动登录
    if (!requiresAuth) {
      if (!loginPromise) {
        loginPromise = userStore.autoLogin().finally(() => { loginPromise = null })
      }
      next()
      return
    }

    // 需要认证但未登录 → 等自动登录完成再决定
    if (!loginPromise) {
      loginPromise = userStore.autoLogin().finally(() => { loginPromise = null })
    }
    loginPromise.then((ok) => {
      if (ok) {
        next()
      } else {
        next({ name: 'Login', query: { redirect: to.fullPath } })
      }
    })
  })
}
