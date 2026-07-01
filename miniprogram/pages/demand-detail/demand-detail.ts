/**
 * 需求详情页
 * 查看求助信息、接单、联系求助人
 */

import { demandService } from '../../services/demand';
import type { Demand } from '../../types/demand';
import { getTrustLevel } from '../../utils/enums';

Page({
  data: {
    /** 需求ID */
    demandId: 0,
    /** 需求详情 */
    demand: null as Demand | null,
    /** 加载状态 */
    loading: true,
    /** 加载是否出错 */
    loadError: false,
    /** 是否已过期 */
    expired: false,
    /** 截止倒计时（剩余毫秒，供 t-count-down 使用） */
    deadlineCountdown: 0,
    /** 接单按钮 loading */
    accepting: false,
    /** 预计算：状态主题 */
    statusTheme: 'default' as string,
    /** 预计算：状态文本标签 */
    statusLabel: '未知' as string,
    /** 预计算：信任等级主题 */
    trustTheme: 'default' as string,
    /** 预计算：信任等级标签 */
    trustLabel: '新人' as string,
    /** 预计算：位置图标 */
    locationIcon: 'check-circle' as string,
    /** 预计算：位置主题 */
    locationTheme: 'success' as string,
    /** 预计算：位置标签 */
    locationLabel: '均可' as string,
  },

  onLoad(options: Record<string, string | undefined>) {
    const id = Number(options.id);
    if (!id) {
      wx.showToast({ title: '参数错误', icon: 'error' });
      wx.navigateBack();
      return;
    }
    this.setData({ demandId: id });
    this.loadDetail();
  },

  onUnload() {
    // 清理资源（倒计时已改用 t-count-down 组件，无需手动清除）
  },

  onShareAppMessage() {
    const { demand } = this.data;
    return {
      title: demand ? `${demand.title} - 搭把手求助` : '搭把手 - 校园互助',
      path: `/pages/demand-detail/demand-detail?id=${this.data.demandId}`,
    };
  },

  // ===== 数据加载 =====

  async loadDetail() {
    try {
      const res = await demandService.getDetail(this.data.demandId);
      const demand = res.data;

      // 计算过期状态 & 倒计时（毫秒，供 t-count-down 使用 #73 修复）
      const now = Date.now();
      // iOS 兼容：将 YYYY-MM-DD 转为 YYYY/MM/DD 再 parse
      const deadlineStr = demand.deadline.replace(/-/g, '/');
      const deadline = new Date(deadlineStr).getTime();
      const expired = now > deadline || isNaN(deadline);
      const deadlineCountdown = expired ? 0 : Math.max(0, deadline - now);

      // 预计算 WXML 派生值，避免嵌套三元
      const STATUS_LABEL_MAP: Record<number, string> = { 0: '已关闭', 1: '待接单', 2: '进行中', 3: '已完成' };
      const statusTheme = demand.status === 1 ? 'warning' : demand.status === 2 ? 'primary' : demand.status === 3 ? 'success' : 'default';
      const statusLabel = STATUS_LABEL_MAP[demand.status] || '未知';
      const trust = getTrustLevel(demand.trustScore || 0);
      const trustTheme = trust.level === '金牌' ? 'success' : trust.level === '靠谱' ? 'primary' : 'default';
      const locType = demand.locationType;
      const locationIcon = locType === 1 ? 'laptop' : locType === 2 ? 'location' : 'check-circle';
      const locationTheme = locType === 1 ? 'warning' : locType === 2 ? 'danger' : 'success';
      const locationLabel = locType === 1 ? '线上' : locType === 2 ? '线下' : '均可';

      this.setData({ demand, expired, deadlineCountdown, statusTheme, statusLabel, trustTheme, trustLabel: trust.label, locationIcon, locationTheme, locationLabel, loading: false, loadError: false });
    } catch (err) {
      console.error('加载需求详情失败:', err);
      // #69 修复：设置错误状态
      this.setData({ loading: false, loadError: true });
    }
  },

  // ===== 图片预览 =====

  onPreviewImage(e: WechatMiniprogram.CustomEvent) {
    const { index } = e.currentTarget.dataset;
    const { demand } = this.data;
    if (!demand?.images?.length) return;
    wx.previewImage({
      urls: demand.images,
      current: demand.images[index] || demand.images[0],
    });
  },

  // ===== 接单 =====

  async onAccept() {
    const { demandId, accepting, expired, demand } = this.data;
    if (accepting || expired) return;
    if (!demand) return;

    wx.showModal({
      title: '确认接单',
      content: `确定要接下「${demand.title}」吗？接单后将使用 ${demand.pointReward} 积分作为担保。`,
      success: async (res) => {
        if (!res.confirm) return;

        this.setData({ accepting: true });
        try {
          await demandService.bid(demandId);
          wx.showToast({ title: '接单成功', icon: 'success' });
          // 刷新详情
          this.loadDetail();
        } catch (err) {
          console.error('接单失败:', err);
          wx.showToast({ title: '接单失败，请重试', icon: 'error' });
        } finally {
          this.setData({ accepting: false });
        }
      },
    });
  },

  // ===== 聊天 =====

  onChat() {
    const { demand } = this.data;
    if (!demand) return;
    wx.navigateTo({
      url: `/pages/chat/chat?targetUserId=${demand.userId}&targetNickname=${encodeURIComponent(demand.nickname)}`,
    });
  },
});
