/**
 * 消息 Tab — 聊天会话列表
 */

import { messageService } from '../../services/message';
import { ensureLogin } from '../../utils/auth';
import type { ChatSession } from '../../types/message';

let _lastLoadTime = 0;
const LOAD_DEBOUNCE = 3000;

Page({
  data: {
    sessions: [] as ChatSession[],
    loading: true,
  },

  async onLoad() {
    _lastLoadTime = Date.now();
    if (!(await ensureLogin())) {
      this.setData({ loading: false });
      return;
    }
    this.loadSessions();
  },

  async onShow() {
    if (Date.now() - _lastLoadTime < LOAD_DEBOUNCE) return;
    _lastLoadTime = Date.now();
    if (!(await ensureLogin())) return;
    this.loadSessions();
  },

  onPullDownRefresh() {
    this.loadSessions().finally(() => wx.stopPullDownRefresh());
  },

  async loadSessions() {
    try {
      const res = await messageService.getSessions();
      // 后端返回 PageResult<ChatSession>（分页结构），取 list 数组
      const list: ChatSession[] = Array.isArray(res.data)
        ? res.data as ChatSession[]
        : (res.data as { list?: ChatSession[] })?.list || [];
      this.setData({ sessions: list, loading: false });
      // 更新全局未读数
      const total = list.reduce((sum, s) => sum + (s.unreadCount || 0), 0);
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
      success: (res) => {
        if (res.confirm) {
          // 后端无删除会话接口，仅从本地列表移除
          const sessions = this.data.sessions.filter((s) => s.id !== sessionId);
          this.setData({ sessions });
          wx.showToast({ title: '已删除', icon: 'success' });
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
