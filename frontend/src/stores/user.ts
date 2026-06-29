import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import type { User } from '@/types'
import { getToken, setToken, removeToken, setUserInfo, getUserInfo, removeUserInfo } from '@/utils/auth'
import { login as loginAPI, getProfile } from '@/api/user'

export const useUserStore = defineStore('user', () => {
  const token = ref<string | null>(getToken())
  const user = ref<User | null>(getUserInfo() as User | null)
  const isLoggedIn = computed(() => !!token.value)

  async function login(username: string, password: string) {
    const result = await loginAPI({ username, password })
    token.value = result.token
    user.value = result.userInfo
    setToken(result.token)
    setUserInfo(result.userInfo)
  }

  async function fetchProfile() {
    const profile = await getProfile()
    user.value = profile
    setUserInfo(profile)
  }

  function logout() {
    token.value = null
    user.value = null
    removeToken()
    removeUserInfo()
  }

  return {
    token,
    user,
    isLoggedIn,
    login,
    fetchProfile,
    logout,
  }
})
