import request from '@/utils/request'
import type { Conversation, ChatMessage, Notification, PageResult, PageParams } from '@/types'

export function getConversations(params?: PageParams): Promise<PageResult<Conversation>> {
  return request.get('/v1/chat/sessions', params)
}

export function getMessages(targetUserId: number, params: PageParams): Promise<PageResult<ChatMessage>> {
  return request.get('/v1/chat/messages', { targetUserId, ...params })
}

export function sendMessage(receiverId: number, content: string, msgType: number = 1): Promise<any> {
  return request.post('/v1/chat/send', { receiverId, content, msgType })
}

export function getNotifications(params: PageParams): Promise<PageResult<Notification>> {
  return request.get('/v1/notifications', params)
}

export function getUnreadCount(): Promise<number> {
  return request.get('/v1/notifications/unread-count')
}

export function markAsRead(id: number): Promise<any> {
  return request.put(`/v1/notifications/${id}/read`)
}

export function markAllAsRead(): Promise<any> {
  return request.put('/v1/notifications/read-all')
}
