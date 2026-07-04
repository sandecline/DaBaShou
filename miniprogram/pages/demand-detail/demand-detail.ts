/**
 * 需求详情页
 * 查看求助信息、接单、联系求助人
 */

import { demandService } from '../../services/demand';
import { orderService } from '../../services/order';
import type { Demand } from '../../types/demand';
import { getTrustLevel } from '../../utils/enums';

const STATUS_LABEL_MAP: Record<number, string> = { 0: '已关闭', 1: '待接单', 2: '进行中', 3: '已完成' };

Page({
  data: {
    demandId: 0,
    demand: null as Demand | null,
    loading: true,
    loadError: false,
    expired: false,
    deadlineCountdown: 0,
    accepting: false,
    /** 选择服务货架弹窗 */
    showShelfPicker: false,
    matchedShelves: [] as Array<{ id: number; title: string; pointPrice: number; nickname: string; matchScore: number }>,
    selectedShelfId: 0,
    statusTheme: 'default' as string,
    statusLabel: '未知' as string,
    trustTheme: 'default' as string,
    trustLabel: '新人' as string,
    locationIcon: 'check-circle' as string,
    locationTheme: 'success' as string,
    locationLabel: '均可' as string,
  },

  onLoad(options: Record<string, string | undefined>) {
    const id = Number(options.id);
    if (!id) {
      wx.showToast({ title: '参数错误', icon: 'error' });
      const pages = getCurrentPages();
      if (pages.length > 1) wx.navigateBack();
      else wx.switchTab({ url: '/pages/index/index' });
      return;
    }
    this.setData({ demandId: id });
    this.loadDetail();
  },

  onUnload() {},

  onShareAppMessage() {
    const { demand } = this.data;
    return {
      title: demand ? `${demand.title} - 搭把手求助` : '搭把手 - 校园互助',
      path: `/pages/demand-detail/demand-detail?id=${this.data.demandId}`,
    };
  },

  async loadDetail() {
    try {
      const res = await demandService.getDetail(this.data.demandId);
      const demand = res.data;

      const now = Date.now();
      const deadlineStr = (demand.deadline || '').split('-').join('/').split('T').join(' ');
      const deadline = new Date(deadlineStr).getTime();
      const expired = now > deadline || isNaN(deadline);
      const deadlineCountdown = expired ? 0 : Math.max(0, deadline - now);

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
      this.setData({ loading: false, loadError: true });
    }
  },

  onPreviewImage(e: WechatMiniprogram.CustomEvent) {
    const { index } = e.currentTarget.dataset;
    const { demand } = this.data;
    if (!demand?.images?.length) return;
    wx.previewImage({
      urls: demand.images,
      current: demand.images[index] || demand.images[0],
    });
  },

  onAccept() {
    const { accepting, expired, demand } = this.data;
    if (accepting || expired) return;
    if (!demand) return;

    const myId = getApp().globalData.userInfo?.id;
    if (demand.userId === myId) {
      wx.showToast({ title: '不能接自己发布的求助', icon: 'error' });
      return;
    }

    this.setData({ accepting: true });
    demandService.match(this.data.demandId, 10).then((matchRes) => {
      const shelves = (matchRes.data || []) as Array<{ id: number; title: string; pointPrice: number; nickname: string; matchScore: number }>;
      if (!shelves.length) {
        wx.showToast({ title: '暂无匹配的服务', icon: 'none' });
        this.setData({ accepting: false });
        return;
      }
      this.setData({
        matchedShelves: shelves,
        selectedShelfId: shelves[0].id,
        showShelfPicker: true,
        accepting: false,
      });
    }).catch((err) => {
      console.error('匹配服务失败:', err);
      wx.showToast({ title: '获取匹配服务失败', icon: 'error' });
      this.setData({ accepting: false });
    });
  },

  onShelfSelect(e: WechatMiniprogram.CustomEvent) {
    this.setData({ selectedShelfId: Number(e.currentTarget.dataset.id) });
  },

  onConfirmAccept() {
    const { demandId, selectedShelfId, demand } = this.data;
    if (!demand) return;

    this.setData({ accepting: true, showShelfPicker: false });
    const idempotentToken = `wx_${Date.now()}_${Math.random().toString(36).slice(2, 10)}`;

    demandService.accept(demandId, selectedShelfId, idempotentToken).then((acceptRes) => {
      const acceptData = acceptRes.data;
      const orderId = orderService.createFromDemand({
        demandId: acceptData.demandId || demandId,
        shelfId: acceptData.shelfId || selectedShelfId,
        remark: acceptData.remark || '',
      });
      return orderId;
    }).then((orderRes) => {
      const orderId = orderRes.data;
      wx.showToast({ title: '接单成功', icon: 'success' });
      this.setData({
        demand: { ...demand, status: 2, statusDesc: '进行中' } as Demand,
        statusTheme: 'primary',
        statusLabel: '进行中',
        accepting: false,
      });
      (this as any)._navTimer = setTimeout(() => {
        const pages = getCurrentPages();
        if (pages.length > 1) wx.navigateBack();
        else wx.switchTab({ url: '/pages/index/index' });
      }, 1200);
    }).catch((err) => {
      console.error('接单失败:', err);
      wx.showToast({ title: '接单失败，请重试', icon: 'error' });
      this.setData({ accepting: false });
    });
  },

  onCancelAccept() {
    this.setData({ showShelfPicker: false });
  },

  onChat() {
    const { demand } = this.data;
    if (!demand) return;
    wx.navigateTo({
      url: `/pages/chat/chat?targetUserId=${demand.userId}&targetNickname=${encodeURIComponent(demand.nickname || '')}`,
    });
  },

  onUnload() {
    const self = this as any;
    if (self._navTimer) clearTimeout(self._navTimer);
  },
});