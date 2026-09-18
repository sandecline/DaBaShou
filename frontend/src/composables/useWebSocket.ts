import { ref } from 'vue'
import { getToken } from '@/utils/auth'
import { useMessageStore } from '@/stores/message'

type MessageCallback = (data: any) => void

const connected = ref(false)
let ws: WebSocket | null = null
let reconnectTimer: ReturnType<typeof setTimeout> | null = null
let heartbeatTimer: ReturnType<typeof setInterval> | null = null
let started = false

const messageCallbacks = new Set<MessageCallback>()

export function onMessage(callback: MessageCallback): () => void {
  messageCallbacks.add(callback)
  return () => {
    messageCallbacks.delete(callback)
  }
}

export function connect() {
  if (ws && (ws.readyState === WebSocket.OPEN || ws.readyState === WebSocket.CONNECTING)) return

  const token = getToken()
  if (!token) return

  const proto = location.protocol === 'https:' ? 'wss:' : 'ws:'
  const url = `${proto}//${location.host}/ws/chat?token=${encodeURIComponent(token)}`

  ws = new WebSocket(url)

  ws.onopen = () => {
    connected.value = true
    useMessageStore().setWsConnected(true)
    startHeartbeat()
  }

  ws.onmessage = (event) => {
    try {
      const msg = JSON.parse(event.data)
      handleMessage(msg)
    } catch {
      // ignore parse errors
    }
  }

  ws.onclose = () => {
    connected.value = false
    useMessageStore().setWsConnected(false)
    stopHeartbeat()
    scheduleReconnect()
  }

  ws.onerror = () => {
    ws?.close()
  }
}

export function disconnect() {
  started = false
  stopHeartbeat()
  if (reconnectTimer) {
    clearTimeout(reconnectTimer)
    reconnectTimer = null
  }
  if (ws) {
    ws.onclose = null
    ws.close()
    ws = null
  }
  connected.value = false
  useMessageStore().setWsConnected(false)
}

export function send(data: Record<string, unknown>) {
  if (ws && connected.value) {
    ws.send(JSON.stringify(data))
  }
}

function handleMessage(msg: any) {
  switch (msg.type) {
    case 'chat':
      for (const cb of messageCallbacks) {
        try {
          cb(msg.data)
        } catch {
          // ignore callback errors
        }
      }
      break
    case 'read':
      for (const cb of messageCallbacks) {
        try {
          cb(msg)
        } catch {
          // ignore
        }
      }
      break
    case 'notification':
      useMessageStore().incrementUnread()
      break
    case 'pong':
      break
  }
}

function startHeartbeat() {
  stopHeartbeat()
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
    if (started) connect()
  }, 5000)
}

/**
 * 开始自动连接（登录后调用一次）
 */
export function startWebSocket() {
  started = true
  connect()
}

/**
 * 停止自动连接（登出时调用）
 */
export function stopWebSocket() {
  disconnect()
}
