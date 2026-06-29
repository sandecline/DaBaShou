/** 消息会话 */
export interface Conversation {
  id: number
  targetUserId: number
  targetUserName: string
  targetAvatar: string
  lastMessage: string
  lastMessageTime: string
  unreadCount: number
}

/** 聊天消息 */
export interface ChatMessage {
  id: number
  senderId: number
  receiverId: number
  content: string
  type: 'text' | 'image'
  createTime: string
  // 前端附加字段
  isMine?: boolean
}

/** 系统通知 */
export interface Notification {
  id: number
  userId: number
  title: string
  content: string
  type: NotificationType
  relatedId: number | null
  isRead: 0 | 1
  createTime: string
}

export type NotificationType = 'order' | 'review' | 'system' | 'warning'

export const NotificationTypeMap: Record<NotificationType, string> = {
  order: '订单通知',
  review: '评价通知',
  system: '系统消息',
  warning: '超时预警',
}
