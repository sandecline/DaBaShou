/**
 * 消息/聊天服务
 * 对应后端 dabashou-message 模块
 * 与 frontend/src/api/message.ts 对齐
 */

import { api } from '../utils/request';
import type { PageResult } from '../types/api-response';
import type { ChatSession, ChatMessage, Notification } from '../types/message';

export interface ChatSendParams {
  receiverId: number;
  content: string;
  msgType: 1 | 2;
}

export const messageService = {
  // ===== 聊天会话 =====

  /** 获取聊天会话列表 */
  getSessions(pageNum = 1, pageSize = 20) {
    return api.get<PageResult<ChatSession>>('/v1/chat/sessions', { pageNum, pageSize } as unknown as Record<string, unknown>);
  },

  /** 创建聊天会话 */
  createSession(userId: number) {
    return api.post<{ id: number }>('/v1/chat/sessions', { userId });
  },

  /** 获取聊天消息（通过对方用户ID） */
  getMessages(targetUserId: number, params: { pageNum: number; pageSize: number }) {
    return api.get<PageResult<ChatMessage>>(
      '/v1/chat/messages',
      { targetUserId, ...params } as unknown as Record<string, unknown>
    );
  },

  /** 获取聊天消息（通过会话ID，用于 targetUserId 未知时的回退） */
  getMessagesBySession(sessionId: number, params: { pageNum: number; pageSize: number }) {
    return api.get<PageResult<ChatMessage>>(
      `/v1/chat/sessions/${sessionId}/messages`,
      params as unknown as Record<string, unknown>
    );
  },

  /** 标记会话已读（通过对方用户ID） */
  markSessionRead(targetUserId: number) {
    return api.put<void>('/v1/chat/messages/read', { targetUserId });
  },

  /** 发送聊天消息（后端返回 Void，不返回消息 ID） */
  sendMessage(params: ChatSendParams) {
    return api.post<void>('/v1/chat/send', params as unknown as Record<string, unknown>);
  },

  // ===== 系统通知 =====

  /** 获取未读消息数 */
  getUnreadCount() {
    return api.get<{ total: number }>('/v1/notifications/unread-count');
  },

  /** 获取系统通知列表 */
  getNotifications(params: { type?: string; isRead?: boolean; pageNum?: number; pageSize?: number }) {
    return api.get<PageResult<Notification>>(
      '/v1/notifications',
      params as unknown as Record<string, unknown>
    );
  },

  /** 标记通知已读 */
  readNotification(notifyId: number) {
    return api.put<void>(`/v1/notifications/${notifyId}/read`);
  },

  /** 标记全部已读 */
  markAllAsRead() {
    return api.put<void>('/v1/notifications/read-all');
  },

  /** 删除通知 */
  deleteNotification(notifyId: number) {
    return api.delete<void>(`/v1/notifications/${notifyId}`);
  },
};
