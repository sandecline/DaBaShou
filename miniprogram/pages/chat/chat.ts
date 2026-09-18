/**
 * 聊天详情页
 * 实时聊天消息、文字/图片发送、自动滚动
 */

import { messageService } from '../../services/message';
import { fileService } from '../../services/file';
import { connect, on, off } from '../../utils/websocket';
import { ensureLogin } from '../../utils/auth';
import type { ChatMessage, ChatSession } from '../../types/message';

Page({
  data: {
    /** 聊天会话ID */
    sessionId: 0,
    /** 对方用户ID */
    targetUserId: 0,
    /** 对方昵称（用于导航栏标题） */
    targetNickname: '',
    /** 对方头像 */
    targetAvatar: '',
    /** 当前用户头像 */
    myAvatar: '',
    /** 消息列表 */
    messages: [] as ChatMessage[],
    /** 加载状态 */
    loading: true,
    /** 输入框内容 */
    inputValue: '',
    /** 发送中 */
    sending: false,
    /** 分页 */
    pageNum: 1,
    pageSize: 20,
    hasMore: true,
    /** 滚动到指定 id */
    scrollIntoId: '',
  },

  messageHandler: null as ((data: unknown) => void) | null,

  async onLoad(options: Record<string, string | undefined>) {
    let sessionId = Number(options.sessionId) || 0;
    let targetUserId = Number(options.targetUserId) || 0;
    let targetNickname = options.targetNickname
      ? decodeURIComponent(options.targetNickname)
      : '';
    let targetAvatar = options.targetAvatar
      ? decodeURIComponent(options.targetAvatar)
      : '';

    // 只有 sessionId 没有 targetUserId 时，从会话列表反查
    if (sessionId && !targetUserId) {
      try {
        const res = await messageService.getSessions(1, 100);
        const list: ChatSession[] = Array.isArray(res.data)
          ? res.data as ChatSession[]
          : (res.data as { list?: ChatSession[] })?.list || [];
        const session = list.find((s) => s.id === sessionId);
        if (session) {
          targetUserId = session.otherUserId;
          targetNickname = targetNickname || session.otherNickname || '';
          targetAvatar = targetAvatar || session.otherAvatar || '';
        }
      } catch { /* ignore */ }
    }

    if (!sessionId && !targetUserId) {
      wx.showToast({ title: '参数错误', icon: 'error' });
      const pages = getCurrentPages();
      if (pages.length > 1) wx.navigateBack();
      else wx.switchTab({ url: '/pages/index/index' });
      return;
    }

    // 若无 sessionId，需先创建会话
    if (!sessionId && targetUserId) {
      try {
        const res = await messageService.createSession(targetUserId);
        sessionId = res.data.id;
      } catch { /* ignore */ }
    }

    this.setData({ sessionId, targetUserId, targetNickname, targetAvatar });

    // 动态设置导航栏标题
    if (targetNickname) {
      wx.setNavigationBarTitle({ title: targetNickname });
    }

    // 确保登录完成后再建立 WebSocket
    await ensureLogin();
    // 设置当前用户头像
    const myAvatar = getApp().globalData.userInfo?.avatar || '';
    this.setData({ myAvatar });

    this.initWebSocket();
    this.loadMessages();
  },

  onUnload() {
    // 取消 WebSocket 消息监听
    if (this.messageHandler) {
      off('chat', this.messageHandler);
      this.messageHandler = null;
    }
  },

  // ===== WebSocket =====

  initWebSocket() {
    // 建立连接（WebSocket 单例，已连接则跳过）
    connect();

    // 监听新消息（后端 REST 保存后自动通过 WS 推送 type=chat）
    this.messageHandler = (data: unknown) => {
      const msg = data as ChatMessage & { type: string };
      const myUserId = getApp().globalData.userInfo?.id;
      if (msg.senderId === myUserId) return; // 忽略自己发的（已本地追加）
      const idx = this.data.messages.length;
      this.setData({
        [`messages[${idx}]`]: { ...msg, isMine: false },
        scrollIntoId: `msg-${msg.id}`,
      });
      // 标记已读（HTTP 调用）
      if (this.data.targetUserId) {
        messageService.markSessionRead(this.data.targetUserId).catch(() => {});
      }
    };
    on('chat', this.messageHandler);
  },

  // ===== 消息加载 =====

  async loadMessages() {
    const { sessionId, targetUserId, pageNum, pageSize } = this.data;

    // 优先用 targetUserId，回退到 sessionId
    if (!targetUserId && !sessionId) {
      this.setData({ loading: false });
      return;
    }

    try {
      const res = targetUserId
        ? await messageService.getMessages(targetUserId, { pageNum, pageSize })
        : await messageService.getMessagesBySession(sessionId, { pageNum, pageSize });
      const newMessages = res.data.list;

      // 通过 sessionId 加载时，尝试解析 targetUserId 以便后续发送
      if (!targetUserId && sessionId && pageNum === 1) {
        const myUserId = getApp().globalData.userInfo?.id;
        const otherMsg = newMessages.find((m) => m.senderId !== myUserId);
        if (otherMsg) {
          this.setData({ targetUserId: otherMsg.senderId });
        }
      }

      // 标记 isMine：对比 senderId 和当前用户ID（后端返回倒序，需反转为正序：旧→新）
      const myUserId = getApp().globalData.userInfo?.id;
      const processed = newMessages.map((msg) => ({
        ...msg,
        isMine: msg.senderId === myUserId,
      })).reverse();

      const messages = pageNum === 1
        ? processed
        : [...processed, ...this.data.messages];

      this.setData({
        messages,
        hasMore: messages.length < res.data.total,
        loading: false,
        pageNum: pageNum + 1, // 仅成功后递增，避免失败时跳过数据页
        scrollIntoId: pageNum === 1 && messages.length > 0
          ? `msg-${messages[messages.length - 1].id}`
          : '',
      });
    } catch (err) {
      console.error('加载聊天记录失败:', err);
      this.setData({ loading: false });
    }
  },

  // 加载更多历史消息
  async loadMore() {
    if (!this.data.hasMore || this.data.loading) return;
    this.setData({ loading: true });
    await this.loadMessages();
    // 仅在加载成功后才递增页码（loadMessages 中已更新 pageNum + hasMore）
  },

  // ===== 消息发送 =====

  onInputChange(e: WechatMiniprogram.Input) {
    // 只存到实例变量，失焦时才同步到 data（减少 setData 频率）
    (this as unknown as Record<string, unknown>)._inputCache = e.detail.value;
  },

  onInputBlur() {
    const v = (this as unknown as Record<string, unknown>)._inputCache;
    if (v !== undefined) this.setData({ inputValue: v as string });
  },

  async onSendText() {
    // 从缓存同步最新输入（用户点发送时 input 可能未失焦）
    const cache = (this as unknown as Record<string, unknown>)._inputCache;
    if (cache !== undefined) this.setData({ inputValue: cache as string });

    const { inputValue, sessionId, sending, targetUserId } = this.data;
    if (!inputValue.trim() || sending) return;
    if (!sessionId) {
      wx.showToast({ title: '会话未就绪', icon: 'error' });
      return;
    }
    if (!targetUserId) {
      wx.showToast({ title: '对方用户未识别', icon: 'error' });
      return;
    }

    this.setData({ sending: true, inputValue: '' });
    try {
      await messageService.sendMessage({ receiverId: this.data.targetUserId, content: inputValue.trim(), msgType: 1 });
      // 直接追加到本地消息列表（不依赖 WS 回显）
      const myUserId = getApp().globalData.userInfo?.id;
      const localMsg: ChatMessage = {
        id: Date.now() + Math.floor(Math.random() * 1000),
        sessionId,
        senderId: myUserId || 0,
        msgType: 1,
        content: inputValue.trim(),
        isMine: true,
        isRead: 0,
        senderAvatar: this.data.myAvatar,
        createTime: new Date().toISOString(),
      };
      const idx = this.data.messages.length;
      this.setData({
        [`messages[${idx}]`]: localMsg,
        scrollIntoId: `msg-${localMsg.id}`,
      });
    } catch (err) {
      console.error('发送消息失败:', err);
      wx.showToast({ title: '发送失败', icon: 'error' });
      // 恢复输入框内容，重置发送标志
      this.setData({ inputValue, sending: false });
    }
  },

  // ===== 图片消息 =====

  onSendImage() {
    const { sessionId, targetUserId } = this.data;
    if (!sessionId) {
      wx.showToast({ title: '会话未就绪', icon: 'error' });
      return;
    }
    if (!targetUserId) {
      wx.showToast({ title: '对方用户未识别', icon: 'error' });
      return;
    }

    wx.chooseMedia({
      count: 1,
      mediaType: ['image'],
      sourceType: ['album', 'camera'],
      success: (res) => {
        const tempFilePath = res.tempFiles[0].tempFilePath;
        this.uploadAndSendImage(tempFilePath);
      },
    });
  },

  async uploadAndSendImage(filePath: string) {
    wx.showLoading({ title: '发送中...' });
    try {
      const imageUrl = await fileService.upload(filePath);

      await messageService.sendMessage({ receiverId: this.data.targetUserId, content: imageUrl, msgType: 2 });
      // 直接追加图片消息到本地列表
      const myUserId = getApp().globalData.userInfo?.id;
      const localMsg: ChatMessage = {
        id: Date.now() + Math.floor(Math.random() * 1000),
        sessionId: this.data.sessionId,
        senderId: myUserId || 0,
        msgType: 2,
        content: imageUrl,
        isMine: true,
        isRead: 0,
        senderAvatar: this.data.myAvatar,
        createTime: new Date().toISOString(),
      };
      const idx = this.data.messages.length;
      this.setData({
        [`messages[${idx}]`]: localMsg,
        scrollIntoId: `msg-${localMsg.id}`,
      });

      wx.hideLoading();
    } catch (err) {
      console.error('图片发送失败:', err);
      wx.hideLoading();
      wx.showToast({ title: '发送失败', icon: 'error' });
    }
  },

  // ===== 图片预览 =====

  onPreviewMessageImage(e: WechatMiniprogram.CustomEvent) {
    const { url } = e.currentTarget.dataset;
    if (url) {
      wx.previewImage({
        urls: [url],
        current: url,
      });
    }
  },

  // ===== 滚动控制 =====

  // 滚动到顶部时加载更多历史消息
  onScrollToUpper() {
    this.loadMore();
  },
});
