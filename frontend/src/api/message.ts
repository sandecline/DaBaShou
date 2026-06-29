import request from '@/utils/request'
import type { Conversation, ChatMessage, Notification, PageResult, PageParams } from '@/types'

export function getConversations(): Promise<Conversation[]> {
  return request.get('/message/conversations')
}

export function getMessages(targetUserId: number, params: PageParams): Promise<PageResult<ChatMessage>> {
  return request.get(`/message/chat/${targetUserId}`, params)
}

export function sendMessage(receiverId: number, content: string, type: 'text' | 'image' = 'text'): Promise<any> {
  return request.post('/message/send', { receiverId, content, type })
}

export function getNotifications(params: PageParams): Promise<PageResult<Notification>> {
  return request.get('/message/notifications', params)
}

export function getUnreadCount(): Promise<{ count: number }> {
  return request.get('/message/unread')
}

export function markAsRead(messageId: number): Promise<any> {
  return request.post('/message/read', { messageId })
}

export function markAllAsRead(): Promise<any> {
  return request.post('/message/read-all')
}
