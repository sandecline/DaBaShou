import { ref, onMounted, onUnmounted } from 'vue'
import { getToken } from '@/utils/auth'
import { useMessageStore } from '@/stores/message'

export function useWebSocket() {
  const ws = ref<WebSocket | null>(null)
  const connected = ref(false)
  const messageStore = useMessageStore()

  let reconnectTimer: ReturnType<typeof setTimeout> | null = null
  let heartbeatTimer: ReturnType<typeof setInterval> | null = null

  function connect() {
    const token = getToken()
    if (!token) return

    const url = `${import.meta.env.VITE_WS_URL || 'ws://localhost:8080/ws'}?token=${token}`
    ws.value = new WebSocket(url)

    ws.value.onopen = () => {
      connected.value = true
      messageStore.setWsConnected(true)
      startHeartbeat()
    }

    ws.value.onmessage = (event) => {
      try {
        const data = JSON.parse(event.data)
        handleMessage(data)
      } catch {
        // ignore parse errors
      }
    }

    ws.value.onclose = () => {
      connected.value = false
      messageStore.setWsConnected(false)
      stopHeartbeat()
      scheduleReconnect()
    }

    ws.value.onerror = () => {
      ws.value?.close()
    }
  }

  function handleMessage(data: any) {
    switch (data.type) {
      case 'chat':
        // Chat message — handled by chat view
        break
      case 'notification':
        messageStore.incrementUnread()
        break
      case 'heartbeat':
        // respond with pong
        break
    }
  }

  function send(data: any) {
    if (ws.value && connected.value) {
      ws.value.send(JSON.stringify(data))
    }
  }

  function startHeartbeat() {
    heartbeatTimer = setInterval(() => {
      send({ type: 'ping' })
    }, 30000)
  }

  function stopHeartbeat() {
    if (heartbeatTimer) {
      clearInterval(heartbeatTimer)
      heartbeatTimer = null
    }
  }

  function scheduleReconnect() {
    if (reconnectTimer) return
    reconnectTimer = setTimeout(() => {
      reconnectTimer = null
      connect()
    }, 5000)
  }

  function disconnect() {
    stopHeartbeat()
    if (reconnectTimer) {
      clearTimeout(reconnectTimer)
      reconnectTimer = null
    }
    ws.value?.close()
  }

  return {
    connected,
    connect,
    disconnect,
    send,
  }
}
