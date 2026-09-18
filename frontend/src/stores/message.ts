import { defineStore } from 'pinia'
import { ref } from 'vue'
import { getUnreadCount } from '@/api/message'
import { getToken } from '@/utils/auth'

export const useMessageStore = defineStore('message', () => {
  const unreadCount = ref(0)
  const wsConnected = ref(false)

  async function fetchUnreadCount() {
    if (!getToken()) {
      unreadCount.value = 0
      return
    }
    try {
      const count = await getUnreadCount()
      unreadCount.value = count
    } catch {
      // ignore
    }
  }

  function setUnreadCount(count: number) {
    unreadCount.value = count
  }

  function incrementUnread() {
    unreadCount.value++
  }

  function decrementUnread(count: number) {
    unreadCount.value = Math.max(unreadCount.value - count, 0)
  }

  function setWsConnected(connected: boolean) {
    wsConnected.value = connected
  }

  return {
    unreadCount,
    wsConnected,
    fetchUnreadCount,
    setUnreadCount,
    incrementUnread,
    decrementUnread,
    setWsConnected,
  }
})
