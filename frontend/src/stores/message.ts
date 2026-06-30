import { defineStore } from 'pinia'
import { ref } from 'vue'
import { getUnreadCount } from '@/api/message'

export const useMessageStore = defineStore('message', () => {
  const unreadCount = ref(0)
  const wsConnected = ref(false)

  async function fetchUnreadCount() {
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

  function setWsConnected(connected: boolean) {
    wsConnected.value = connected
  }

  return {
    unreadCount,
    wsConnected,
    fetchUnreadCount,
    setUnreadCount,
    incrementUnread,
    setWsConnected,
  }
})
