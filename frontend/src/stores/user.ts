import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { getToken, setToken, removeToken, setUserInfo, getUserInfo, removeUserInfo } from '@/utils/auth'
import { login as loginAPI, getProfile } from '@/api/auth'

export const useUserStore = defineStore('user', () => {
  const token = ref<string | null>(getToken())
  const user = ref<any | null>(getUserInfo())
  const isLoggedIn = computed(() => !!token.value)
  const loginReady = ref(false)

  async function login(username: string, password: string) {
    const res = await loginAPI({ username, password })
    // response interceptor returns full {code, msg, data} wrapper
    const result = res.data
    const accessToken = result.accessToken
    const userInfo = { id: result.userId, nickname: result.nickname, avatar: result.avatar }
    token.value = accessToken
    user.value = userInfo
    setToken(accessToken)
    setUserInfo(userInfo)
  }

  async function fetchProfile() {
    const res = await getProfile()
    const profile = res.data
    user.value = profile
    setUserInfo(profile)
  }

  async function autoLogin(): Promise<boolean> {
    if (token.value) {
      loginReady.value = true
      return true
    }
    try {
      await login('zhangsan', '123456')
      loginReady.value = true
      return true
    } catch {
      loginReady.value = true
      return false
    }
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
    loginReady,
    login,
    fetchProfile,
    autoLogin,
    logout,
  }
})
