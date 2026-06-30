import { defineStore } from 'pinia'

export const useAppStore = defineStore('app', {
  state: () => ({
    /** 系统通知未读数 */
    unreadCount: 0,
    /** 全局 loading */
    loading: false,
    /** 技能分类缓存（首页用） */
    categories: [],
  }),

  actions: {
    setUnreadCount(count) {
      this.unreadCount = count
    },

    setLoading(v) {
      this.loading = v
    },

    setCategories(cats) {
      this.categories = cats
    },
  },
})
