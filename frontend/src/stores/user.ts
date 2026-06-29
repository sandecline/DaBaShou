import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import type { User } from '@/types'
import { getToken, setToken, removeToken, setUserInfo, getUserInfo, removeUserInfo } from '@/utils/auth'
import { login as loginAPI, getProfile } from '@/api/user'

export const useUserStore = defineStore('user', () => {
  const token = ref<string | null>(getToken())
  const user = ref<User | null>(normalizeUser(getUserInfo() as any))
  const isLoggedIn = computed(() => !!token.value)

  // 兼容 snake_case 和 camelCase 的用户数据
  function normalizeUser(u: any): User | null {
    if (!u) return null
    return {
      id: u.id ?? 0,
      username: u.username ?? '',
      nickname: u.nickname ?? '',
      avatar: u.avatar ?? '',
      phone: u.phone ?? '',
      email: u.email ?? '',
      pointBalance: u.pointBalance ?? u.point_balance ?? 0,
      trustScore: u.trustScore ?? u.trust_score ?? 5.0,
      longitude: u.longitude ?? 0,
      latitude: u.latitude ?? 0,
      campus: u.campus ?? '',
      building: u.building ?? '',
      bio: u.bio ?? '',
      status: u.status ?? 1,
      createTime: u.createTime ?? u.create_time ?? '',
      updateTime: u.updateTime ?? u.update_time ?? '',
    }
  }

  async function login(username: string, password: string) {
    const result = await loginAPI({ username, password })
    token.value = result.token
    user.value = normalizeUser(result.userInfo)
    setToken(result.token)
    setUserInfo(normalizeUser(result.userInfo))
  }

  async function fetchProfile() {
    const profile = await getProfile()
    user.value = normalizeUser(profile)
    setUserInfo(normalizeUser(profile))
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
