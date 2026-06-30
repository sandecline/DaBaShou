import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import type { UserProfileVo, LoginVo } from '@/types/api'
import { getToken, setToken, removeToken, getUserInfo, setUserInfo, removeUserInfo } from '@/utils/auth'
import { login as loginAPI, getProfile } from '@/api/auth'

export const useUserStore = defineStore('user', () => {
  const token = ref<string | null>(getToken())
  const user = ref<UserProfileVo | null>(getUserInfo() as UserProfileVo | null)
  const isLoggedIn = computed(() => !!token.value)

  async function login(username: string, password: string) {
    logout()
    const result: LoginVo = await loginAPI({ username, password })
    token.value = result.accessToken
    setToken(result.accessToken)
    user.value = {
      id: result.userId,
      username,
      nickname: result.nickname,
      avatar: result.avatar,
    } as UserProfileVo
    setUserInfo(user.value as any)
    fetchProfile().catch(() => {
      // 登录已成功，个人资料补拉失败不阻塞进入页面；后续受保护接口会按401统一处理。
    })
  }

  async function fetchProfile() {
    const profile: UserProfileVo = await getProfile()
    user.value = profile
    setUserInfo(profile as any)
  }

  function logout() {
    token.value = null
    user.value = null
    removeToken()
    removeUserInfo()
  }

  return { token, user, isLoggedIn, login, fetchProfile, logout }
})
