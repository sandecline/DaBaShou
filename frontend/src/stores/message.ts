import { defineStore } from 'pinia'
import { ref } from 'vue'
import { getUnreadCount } from '@/api/message'

export const useMessageStore = defineStore('message', () => {
  const unreadCount = ref(0)
  const wsConnected = ref(false)

  async function fetchUnreadCount() {
    try {
      const result = await getUnreadCount()
      // mock returns { code, msg, data } where data is number
      if (typeof result === 'number') {
        unreadCount.value = result
      } else if (result && typeof (result as any).count === 'number') {
        unreadCount.value = (result as any).count
      } else if (result && typeof (result as any).data === 'number') {
        unreadCount.value = (result as any).data
      }
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
