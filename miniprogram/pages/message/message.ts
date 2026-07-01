/**
 * 消息 Tab — 聊天会话列表
 */

import { messageService } from '../../services/message';
import type { ChatSession } from '../../types/message';

Page({
  data: {
    sessions: [] as ChatSession[],
    loading: true,
  },

  onLoad() {
    this.loadSessions();
  },

  onShow() {
    this.loadSessions();
  },

  onPullDownRefresh() {
    this.loadSessions().finally(() => wx.stopPullDownRefresh());
  },

  async loadSessions() {
    try {
      const res = await messageService.getSessions();
      // 后端可能返回数组,也可能返回 { list, total } 等分页结构;统一兜底为数组
      const sessions = Array.isArray(res.data)
        ? res.data
        : Array.isArray((res.data as { list?: ChatSession[] })?.list)
          ? (res.data as { list: ChatSession[] }).list
          : [];
      this.setData({ sessions, loading: false });
      // 更新全局未读数
      const total = sessions.reduce((sum, s) => sum + (s.unreadCount || 0), 0);
      getApp().globalData.unreadCount = total;
    } catch (err) {
      console.error('加载会话列表失败:', err);
      this.setData({ loading: false });
    }
  },

  goChat(e: WechatMiniprogram.TouchEvent) {
    const { id } = e.currentTarget.dataset;
    wx.navigateTo({ url: `/pages/chat/chat?sessionId=${id}` });
  },

  onDelete(e: WechatMiniprogram.TouchEvent) {
    const sessionId = Number(e.currentTarget.dataset.id);
    wx.showModal({
      title: '删除会话',
      content: '确定删除此会话？',
      success: async (res) => {
        if (res.confirm) {
          try {
            // #51 修复：调用真实的删除 API
            await messageService.deleteSession(sessionId);
            wx.showToast({ title: '已删除', icon: 'success' });
            // 从本地列表移除并刷新
            this.loadSessions();
          } catch (err) {
            console.error('删除会话失败:', err);
            wx.showToast({ title: '删除失败', icon: 'error' });
          }
        }
      },
    });
  },

  onShareAppMessage() {
    return {
      title: '搭把手 - 消息',
      path: '/pages/message/message',
    };
  },
});
