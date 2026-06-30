import { defineStore } from 'pinia'
import { isLoggedIn, saveToken, clearToken } from '@/utils/auth'
import * as authApi from '@/utils/auth'

export const useUserStore = defineStore('user', {
  state: () => ({
    token: uni.getStorageSync('access_token') || '',
    profile: null,
    pointBalance: 0,
  }),

  getters: {
    isLogin: (state) => !!state.token,
    nickname: (state) => state.profile?.nickname || '',
    avatar: (state) => state.profile?.avatar || '',
  },

  actions: {
    /** 保存 Token */
    setToken(token) {
      this.token = token
      saveToken(token)
    },

    /** 设置用户信息 */
    setProfile(profile) {
      this.profile = profile
    },

    /** 设置积分余额 */
    setPointBalance(balance) {
      this.pointBalance = balance
    },

    /** 微信登录 */
    async loginWithWechat() {
      const user = await authApi.wechatLogin()
      this.setToken(user.accessToken)
      this.profile = user.user
      return user
    },

    /** 账号密码登录 */
    async loginWithPassword(username, password) {
      const data = await authApi.loginByPassword(username, password)
      this.setToken(data.accessToken)
      this.profile = data
      return data
    },

    /** 退出登录 */
    logout() {
      this.token = ''
      this.profile = null
      this.pointBalance = 0
      clearToken()
    },

    /** 刷新用户信息 */
    async refreshProfile() {
      const { getProfile } = await import('@/services/user')
      this.profile = await getProfile()
    },
  },
})
