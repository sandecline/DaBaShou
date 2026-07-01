/**
 * 聊天详情页
 * 实时聊天消息、文字/图片发送、自动滚动
 */

import { messageService } from '../../services/message';
import { connect, on, off, send } from '../../utils/websocket';
import { ensureLogin } from '../../utils/auth';
import type { ChatMessage } from '../../types/message';

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
    const sessionId = Number(options.sessionId);
    const targetUserId = Number(options.targetUserId);
    const targetNickname = options.targetNickname
      ? decodeURIComponent(options.targetNickname)
      : '';
    const targetAvatar = options.targetAvatar
      ? decodeURIComponent(options.targetAvatar)
      : '';

    if (sessionId) {
      this.setData({ sessionId, targetUserId, targetNickname, targetAvatar });
    } else if (targetUserId) {
      this.setData({ targetUserId, targetNickname, targetAvatar });
      // TODO: 若无 sessionId，需先创建会话再拉取消息
    } else {
      wx.showToast({ title: '参数错误', icon: 'error' });
      wx.navigateBack();
      return;
    }

    // 动态设置导航栏标题
    if (targetNickname) {
      wx.setNavigationBarTitle({ title: targetNickname });
    }

    // 确保登录完成后再建立 WebSocket（#78 修复）
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
      off('new_message', this.messageHandler);
      this.messageHandler = null;
    }
  },

  // ===== WebSocket =====

  initWebSocket() {
    // 建立连接（WebSocket 单例，已连接则跳过）
    connect();

    // 监听新消息
    this.messageHandler = (data: unknown) => {
      const msg = data as ChatMessage & { type: string };
      if (msg.type === 'new_message' && msg.sessionId === this.data.sessionId) {
        // 增量追加，避免传输整个数组
        const idx = this.data.messages.length;
        this.setData({
          [`messages[${idx}]`]: msg,
          scrollIntoId: `msg-${msg.id}`,
        });
        // 标记已读
        messageService.readSession(this.data.sessionId).catch(() => {});
      }
    };
    on('new_message', this.messageHandler);
  },

  // ===== 消息加载 =====

  async loadMessages() {
    const { sessionId, pageNum, pageSize } = this.data;
    if (!sessionId) {
      // 无 sessionId 时，暂且不加载（后续创建会话后加载）
      this.setData({ loading: false });
      return;
    }

    try {
      const res = await messageService.getMessages(sessionId, { pageNum, pageSize });
      const newMessages = res.data.list;

      // 标记 isMine：对比 senderId 和当前用户ID
      const myUserId = getApp().globalData.userInfo?.id;
      const processed = newMessages.map((msg) => ({
        ...msg,
        isMine: msg.senderId === myUserId,
      }));

      const messages = pageNum === 1
        ? processed
        : [...processed, ...this.data.messages];

      this.setData({
        messages,
        hasMore: messages.length < res.data.total,
        loading: false,
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
    this.setData({
      loading: true,
      pageNum: this.data.pageNum + 1,
    });
    await this.loadMessages();
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
    const { inputValue, sessionId, sending } = this.data;
    if (!inputValue.trim() || sending) return;
    if (!sessionId) {
      wx.showToast({ title: '会话未就绪', icon: 'error' });
      return;
    }

    this.setData({ sending: true, inputValue: '' });
    try {
      const res = await messageService.sendMessage(sessionId, inputValue.trim(), 1);
      // 直接追加到本地消息列表（#79 修复：不依赖 WS 回显）
      const myUserId = getApp().globalData.userInfo?.id;
      const localMsg: ChatMessage = {
        id: res.data.id,
        sessionId,
        senderId: myUserId || 0,
        msgType: 1,
        content: inputValue.trim(),
        isMine: true,
        senderAvatar: this.data.myAvatar,
        createTime: new Date().toISOString(),
      };
      const idx = this.data.messages.length;
      this.setData({
        [`messages[${idx}]`]: localMsg,
        scrollIntoId: `msg-${localMsg.id}`,
      });
      // 通过 WebSocket 推送消息给服务器
      send('chat_message', {
        sessionId,
        content: inputValue.trim(),
        msgType: 1,
        messageId: res.data.id,
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
    const { sessionId } = this.data;
    if (!sessionId) {
      wx.showToast({ title: '会话未就绪', icon: 'error' });
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
      // TODO: 替换为真实图片上传地址，当前使用 Base URL + upload 路径 (#80 修复)
      const uploadRes = await wx.uploadFile({
        url: `${getApp().globalData.apiBaseUrl || 'https://api.dabashou.example.com'}/api/v1/upload/image`,
        filePath,
        name: 'file',
        header: {
          Authorization: `Bearer ${getApp().globalData.token || ''}`,
        },
      });

      // #81 修复：JSON.parse 加 try-catch
      let imageUrl: string;
      try {
        const parsed = JSON.parse(uploadRes.data);
        imageUrl = parsed.data as string;
      } catch {
        console.error('[Chat] 上传响应解析失败:', uploadRes.data);
        throw { code: -1, msg: '上传响应解析失败' };
      }

      const res = await messageService.sendMessage(this.data.sessionId, imageUrl, 2);
      // 直接追加图片消息到本地列表
      const myUserId = getApp().globalData.userInfo?.id;
      const localMsg: ChatMessage = {
        id: res.data.id,
        sessionId: this.data.sessionId,
        senderId: myUserId || 0,
        msgType: 2,
        content: imageUrl,
        isMine: true,
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

  onScrollToLower() {
    // 滚动到底部时加载更多
    this.loadMore();
  },
});
