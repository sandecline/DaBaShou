export type MsgType = 1 | 2;

export interface ChatSessionVo {
  id: number;
  otherUserId: number;
  otherNickname: string;
  otherAvatar: string;
  lastMessage: string;
  lastTime: string;
  unreadCount: number;
}

export interface ChatMessageVo {
  id: number;
  senderId: number;
  senderNickname: string;
  senderAvatar: string;
  content: string;
  msgType: number;
  isRead: number;
  createTime: string;
  isMine?: boolean;
}

export interface NotificationVo {
  id: number;
  type: string;
  title: string;
  content: string;
  relatedType: string;
  relatedId: number;
  isRead: number;
  readTime: string | null;
  createTime: string;
}

export type { ChatSessionVo as ChatSession };
export type { ChatMessageVo as ChatMessage };
export type { NotificationVo as Notification };
