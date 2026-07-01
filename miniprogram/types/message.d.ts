/**
 * 消息相关类型定义
 * 对应后端 dabashou-message 模块
 * 与 frontend/src/types/message.ts + api.ts 保持一致
 */

/** 消息类型 */
export type MsgType = 1 | 2;
// 1-文字  2-图片

/** 通知类型（与前端一致） */
export type NotificationType = 'order' | 'review' | 'system' | 'warning';

/** 聊天会话（与前端 Conversation/ChatSessionVo 一致） */
export interface ChatSession {
  id: number;
  /** 对方用户ID */
  otherUserId: number;
  /** 对方昵称 */
  otherNickname: string;
  /** 对方头像 */
  otherAvatar?: string;
  /** 最后一条消息 */
  lastMessage: string;
  /** 最后消息时间 */
  lastTime?: string;
  lastMessageTime?: string;
  /** 未读消息数 */
  unreadCount: number;
  /** 关联订单（如有） */
  orderId?: number;
  orderTitle?: string;
}

/** 聊天消息（与前端 ChatMessage/ChatMessageVo 一致） */
export interface ChatMessage {
  id: number;
  senderId: number;
  senderNickname?: string;
  senderAvatar?: string;
  content: string;
  msgType: MsgType;
  isRead: 0 | 1;
  createTime: string;
  /** 前端标记：是否为当前用户发送 */
  isMine?: boolean;
}

/** 系统通知（与前端 Notification/NotificationVo 一致） */
export interface Notification {
  id: number;
  type: NotificationType;
  title: string;
  content: string;
  /** 关联业务 */
  relatedType: string;
  relatedId?: number;
  isRead: boolean;
  readTime?: string | null;
  createTime: string;
}
