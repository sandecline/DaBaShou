import { get, post, put, del } from '@/utils/request'

export function getNotifications(params) { return get('/notifications', params) }

export function getUnreadCount() { return get('/notifications/unread-count') }

export function markRead(id) { return put('/notifications/' + id + '/read') }

export function markAllRead() { return put('/notifications/read-all') }

export function deleteNotification(id) { return del('/notifications/' + id) }

export function getChatSessions() { return get('/chat/sessions') }

export function createSession(userId) { return post('/chat/sessions', { userId }) }

export function getMessages(sessionId, params) { return get('/chat/sessions/' + sessionId + '/messages', params) }
