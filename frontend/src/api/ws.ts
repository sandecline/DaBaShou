type MessageHandler = (data: any) => void

interface WsOptions {
  url: string
  token: string
  onMessage?: MessageHandler
  onOpen?: () => void
  onClose?: () => void
  onError?: (err: Event) => void
  reconnectInterval?: number
  maxReconnectAttempts?: number
}

export class ChatWebSocket {
  private ws: WebSocket | null = null
  private url: string
  private token: string
  private onMessage?: MessageHandler
  private onOpen?: () => void
  private onClose?: () => void
  private onError?: (err: Event) => void
  private reconnectInterval: number
  private maxReconnectAttempts: number
  private reconnectAttempts = 0
  private heartbeatTimer: ReturnType<typeof setInterval> | null = null
  private isManualClose = false

  constructor(options: WsOptions) {
    this.url = options.url
    this.token = options.token
    this.onMessage = options.onMessage
    this.onOpen = options.onOpen
    this.onClose = options.onClose
    this.onError = options.onError
    this.reconnectInterval = options.reconnectInterval ?? 5000
    this.maxReconnectAttempts = options.maxReconnectAttempts ?? 10
  }

  connect(): void {
    if (this.ws?.readyState === WebSocket.OPEN) return

    this.isManualClose = false
    const wsUrl = `${this.url}?token=${encodeURIComponent(this.token)}`
    this.ws = new WebSocket(wsUrl)

    this.ws.onopen = () => {
      this.reconnectAttempts = 0
      this.startHeartbeat()
      this.onOpen?.()
    }

    this.ws.onmessage = (event) => {
      try {
        const data = JSON.parse(event.data)
        if (data.type === 'pong') return
        this.onMessage?.(data)
      } catch {
        this.onMessage?.(event.data)
      }
    }

    this.ws.onclose = () => {
      this.stopHeartbeat()
      this.onClose?.()
      if (!this.isManualClose) {
        this.reconnect()
      }
    }

    this.ws.onerror = (err) => {
      this.onError?.(err)
    }
  }

  send(data: Record<string, any>): void {
    if (this.ws?.readyState === WebSocket.OPEN) {
      this.ws.send(JSON.stringify(data))
    }
  }

  sendChatMessage(sessionId: number, content: string, msgType = 1): void {
    this.send({ type: 'chat', sessionId, content, msgType })
  }

  markRead(sessionId: number): void {
    this.send({ type: 'read', sessionId })
  }

  close(): void {
    this.isManualClose = true
    this.stopHeartbeat()
    this.ws?.close()
    this.ws = null
  }

  private startHeartbeat(): void {
    this.stopHeartbeat()
    this.heartbeatTimer = setInterval(() => {
      this.send({ type: 'ping' })
    }, 30000)
  }

  private stopHeartbeat(): void {
    if (this.heartbeatTimer) {
      clearInterval(this.heartbeatTimer)
      this.heartbeatTimer = null
    }
  }

  private reconnect(): void {
    if (this.reconnectAttempts >= this.maxReconnectAttempts) return
    this.reconnectAttempts++
    setTimeout(() => this.connect(), this.reconnectInterval)
  }
}

export function createChatWs(token: string, onMessage: MessageHandler): ChatWebSocket {
  const protocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:'
  const host = window.location.host
  return new ChatWebSocket({
    url: `${protocol}//${host}/ws/chat`,
    token,
    onMessage,
  })
}
