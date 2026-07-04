import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import type { UserProfileVo, LoginVo } from '@/types/api'
import { getToken, setToken, removeToken, getUserInfo, setUserInfo, removeUserInfo } from '@/utils/auth'
import { login as loginAPI } from '@/api/auth'
import { getProfile } from '@/api/user'

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
    fetchProfile().catch((err) => {
      console.warn('[user] fetchProfile failed after login, non-blocking:', err)
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
