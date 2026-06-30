import request from '@/utils/request'
import type { NotificationVo, ChatSessionVo, ChatMessageVo, PageResult, PageParams } from '@/types/api'
import { normalizePageParams } from './_params'

export function getNotifications(params: PageParams & { type?: string; isRead?: boolean }): Promise<PageResult<NotificationVo>> {
  return request.get('/v1/notifications', normalizePageParams(params))
}

export function getUnreadCount(): Promise<number> {
  return request.get('/v1/notifications/unread-count')
}

export function markAsRead(id: number): Promise<null> {
  return request.put('/v1/notifications/' + id + '/read')
}

export function markAllAsRead(): Promise<null> {
  return request.put('/v1/notifications/read-all')
}

export function deleteNotification(id: number): Promise<null> {
  return request.delete('/v1/notifications/' + id)
}

export function getChatSessions(params?: PageParams): Promise<PageResult<ChatSessionVo>> {
  return request.get('/v1/chat/sessions', normalizePageParams(params))
}

export function getChatMessages(targetUserId: number, params?: PageParams): Promise<PageResult<ChatMessageVo>> {
  return request.get('/v1/chat/messages', normalizePageParams({ targetUserId, ...params }))
}

export function createChatSession(userId: number): Promise<number> {
  return request.post('/v1/chat/sessions', { userId })
}

export function sendChatMessage(receiverId: number, content: string, msgType: number = 1): Promise<null> {
  return request.post('/v1/chat/send', { receiverId, content, msgType })
}
