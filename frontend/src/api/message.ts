import request from '@/utils/request'
import type { ApiResponse, PageResult, NotificationVo, ChatSessionVo, ChatMessageVo } from '@/types/api'

export function getNotifications(params?: {
  type?: string
  isRead?: boolean
  pageNum?: number
  pageSize?: number
}): Promise<ApiResponse<PageResult<NotificationVo>>> {
  return request({ url: '/v1/notifications', method: 'get', params })
}

export function getUnreadCount(): Promise<ApiResponse<number>> {
  return request({ url: '/v1/notifications/unread-count', method: 'get' })
}

export function markAsRead(id: number): Promise<ApiResponse<null>> {
  return request({ url: `/v1/notifications/${id}/read`, method: 'put' })
}

export function markAllAsRead(): Promise<ApiResponse<null>> {
  return request({ url: '/v1/notifications/read-all', method: 'put' })
}

export function deleteNotification(id: number): Promise<ApiResponse<null>> {
  return request({ url: `/v1/notifications/${id}`, method: 'delete' })
}

export function getChatSessions(params?: {
  pageNum?: number
  pageSize?: number
}): Promise<ApiResponse<PageResult<ChatSessionVo>>> {
  return request({ url: '/v1/chat/sessions', method: 'get', params })
}

export function getChatHistory(sessionId: number, params?: {
  pageNum?: number
  pageSize?: number
}): Promise<ApiResponse<PageResult<ChatMessageVo>>> {
  return request({ url: `/v1/chat/sessions/${sessionId}/messages`, method: 'get', params })
}

export function createSession(userId: number): Promise<ApiResponse<number>> {
  return request({ url: '/v1/chat/sessions', method: 'post', data: { userId } })
}

export function getConversations(params?: any): Promise<ApiResponse<any>> {
  return request({ url: '/v1/chat/sessions', method: 'get', params })
}

export function getMessages(sessionId: number, params?: any): Promise<ApiResponse<any>> {
  return request({ url: `/v1/chat/sessions/${sessionId}/messages`, method: 'get', params })
}

export function sendMessage(data: any): Promise<ApiResponse<null>> {
  return request({ url: '/v1/chat/send', method: 'post', data })
}
