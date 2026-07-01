/**
 * WebSocket 连接管理
 * 基于 wx.connectSocket 封装
 * 功能：心跳保活、断线重连、消息分发
 */

// TODO: 替换为真实 WebSocket 地址
const WS_URL = 'wss://api.dabashou.example.com/ws/chat';

// 与 request.ts 保持一致：后端未就绪时跳过真实 WS 连接
const MOCK_MODE = true;

/** 重连配置 */
const RECONNECT_MAX_RETRIES = 5;
const RECONNECT_BASE_DELAY = 1000; // 1s
const HEARTBEAT_INTERVAL = 30000;  // 30s

/** 消息处理器 */
type MessageHandler = (data: unknown) => void;

interface WsState {
  /** WebSocket 任务 */
  task: WechatMiniprogram.SocketTask | null;
  /** 是否已连接 */
  connected: boolean;
  /** 重连次数 */
  reconnectCount: number;
  /** 心跳定时器 */
  heartbeatTimer: number | null;
  /** 重连定时器 */
  reconnectTimer: ReturnType<typeof setTimeout> | null;
  /** 消息处理器映射 */
  handlers: Map<string, MessageHandler[]>;
  /** 待发送消息队列（断线暂存） */
  pendingQueue: { type: string; data: unknown }[];
}

const state: WsState = {
  task: null,
  connected: false,
  reconnectCount: 0,
  heartbeatTimer: null,
  reconnectTimer: null,
  handlers: new Map(),
  pendingQueue: [],
};

/**
 * 建立 WebSocket 连接
 */
export function connect(): void {
  // Mock 模式：无真实后端，跳过连接（聊天发送走 HTTP mock）
  if (MOCK_MODE) {
    console.log('[WS] Mock 模式，跳过连接');
    return;
  }

  if (state.task && state.connected) {
    console.log('[WS] 已连接，跳过');
    return;
  }

  const token = wx.getStorageSync('access_token');
  if (!token) {
    console.warn('[WS] 未登录，跳过连接');
    return;
  }

  const task = wx.connectSocket({
    url: WS_URL,
    header: {
      Authorization: `Bearer ${token}`,
    },
    success() {
      console.log('[WS] 连接发起成功');
    },
    fail(err) {
      console.error('[WS] 连接发起失败:', err);
      scheduleReconnect();
    },
  });

  state.task = task;

  // 连接打开
  task.onOpen(() => {
    console.log('[WS] 连接已打开');
    state.connected = true;
    state.reconnectCount = 0;
    startHeartbeat();
    flushPendingQueue();
  });

  // 接收消息
  task.onMessage((res) => {
    try {
      const msg = JSON.parse(res.data as string);
      dispatchMessage(msg);
    } catch (err) {
      console.error('[WS] 消息解析失败:', err);
    }
  });

  // 连接关闭
  task.onClose((res) => {
    console.log('[WS] 连接关闭:', res.code, res.reason);
    state.connected = false;
    stopHeartbeat();
    scheduleReconnect();
  });

  // 连接错误
  task.onError((err) => {
    console.error('[WS] 连接错误:', err);
  });
}

/**
 * 发送消息（高层接口，断线时暂存到队列）
 */
export function send(type: string, data: unknown): void {
  // Mock 模式：直接丢弃（发送已通过 HTTP 完成）
  if (MOCK_MODE) {
    console.log('[WS] Mock 模式，跳过发送:', type);
    return;
  }

  const payload = JSON.stringify({ type, data });

  if (state.connected && state.task) {
    state.task.send({ data: payload });
  } else {
    // 断线时暂存，不在此处触发重连（避免心跳→send→connect→flush→send的循环）
    console.log('[WS] 暂存消息:', type);
    state.pendingQueue.push({ type, data });
  }
}

/**
 * 注册消息处理器
 */
export function on(type: string, handler: MessageHandler): void {
  const handlers = state.handlers.get(type) || [];
  handlers.push(handler);
  state.handlers.set(type, handlers);
}

/**
 * 移除消息处理器
 */
export function off(type: string, handler: MessageHandler): void {
  const handlers = state.handlers.get(type) || [];
  state.handlers.set(
    type,
    handlers.filter((h) => h !== handler)
  );
}

/**
 * 关闭连接
 */
export function disconnect(): void {
  // 取消重连定时器
  if (state.reconnectTimer) {
    clearTimeout(state.reconnectTimer);
    state.reconnectTimer = null;
  }
  if (state.task) {
    state.task.close({ code: 1000, reason: 'Normal closure' });
    state.task = null;
  }
  state.connected = false;
  stopHeartbeat();
  state.reconnectCount = RECONNECT_MAX_RETRIES; // 禁止重连
}

// ===== 内部方法 =====

/** 心跳 */
function startHeartbeat(): void {
  stopHeartbeat();
  state.heartbeatTimer = setInterval(() => {
    if (state.connected && state.task) {
      // 直接通过底层连接发送，不经过 send() 避免递归重连
      state.task.send({
        data: JSON.stringify({ type: 'ping', data: { timestamp: Date.now() } }),
      });
    }
  }, HEARTBEAT_INTERVAL) as unknown as number;
}

function stopHeartbeat(): void {
  if (state.heartbeatTimer) {
    clearInterval(state.heartbeatTimer);
    state.heartbeatTimer = null;
  }
}

/** 断线重连（指数退避） */
function scheduleReconnect(): void {
  if (state.reconnectCount >= RECONNECT_MAX_RETRIES) {
    console.warn('[WS] 重连次数用尽，停止重连');
    return;
  }

  const delay = RECONNECT_BASE_DELAY * Math.pow(2, state.reconnectCount);
  state.reconnectCount++;
  console.log(`[WS] ${delay}ms 后第 ${state.reconnectCount} 次重连...`);
  state.reconnectTimer = setTimeout(() => connect(), delay);
}

/** 消息分发 */
function dispatchMessage(msg: { type: string; data: unknown }): void {
  const handlers = state.handlers.get(msg.type) || [];
  for (const handler of handlers) {
    try {
      handler(msg.data);
    } catch (err) {
      console.error('[WS] 消息处理异常:', err);
    }
  }
}

/** 发送暂存消息（直接通过底层连接发送，避免递归） */
function flushPendingQueue(): void {
  if (!state.task || !state.connected) return;
  while (state.pendingQueue.length > 0) {
    const item = state.pendingQueue.shift()!;
    state.task.send({ data: JSON.stringify({ type: item.type, data: item.data }) });
  }
}
