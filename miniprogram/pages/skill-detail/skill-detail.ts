/**
 * 技能详情页
 * 查看技能信息、下单、联系卖主
 */

import { shelfService } from '../../services/shelf';
import { orderService } from '../../services/order';
import type { SkillShelf, TimeSlot } from '../../types/shelf';
import type { CreateFromShelfParams } from '../../services/order';
import { getTrustLevel } from '../../utils/enums';
import { getCurrentUserId } from '../../utils/auth';

Page({
  data: {
    /** 技能ID */
    skillId: 0,
    /** 技能详情 */
    skill: null as SkillShelf | null,
    /** 加载状态 */
    loading: true,
    /** 加载是否出错 */
    loadError: false,
    /** 图片预览索引 */
    previewIndex: 0,
    /** 下单按钮 loading */
    ordering: false,
    /** 下单弹窗是否显示 */
    showOrderDialog: false,
    /** 下单备注 */
    orderRemark: '',
    /** 当前用户是否已下单 */
    hasOrdered: false,
    /** 是否为当前用户自己的发布 */
    isOwner: false,
    /** 预计算：信任等级主题 */
    trustTheme: 'default' as string,
    /** 预计算：信任等级标签 */
    trustLabel: '新人' as string,
    /** 预计算：位置类型图标名 */
    locationIcon: 'check-circle' as string,
    /** 预计算：位置类型标签主题 */
    locationTheme: 'success' as string,
    /** 预计算：位置类型标签文本 */
    locationLabel: '均可' as string,
    /** 可用时段列表 */
    availableSlots: [] as TimeSlot[],
    /** 选中的时段ID */
    selectedSlotId: 0,
  },

  onLoad(options: Record<string, string | undefined>) {
    const id = Number(options.id);
    if (!id) {
      wx.showToast({ title: '参数错误', icon: 'error' });
      // 安全返回：检查页面栈，防止无栈时直接 navigateBack
      const pages = getCurrentPages();
      if (pages.length > 1) wx.navigateBack();
      else wx.switchTab({ url: '/pages/index/index' });
      return;
    }
    this.setData({ skillId: id });
    this.loadDetail();
  },

  onShareAppMessage() {
    const { skill } = this.data;
    return {
      title: skill ? `${skill.title} - 搭把手` : '搭把手 - 校园技能共享',
      path: `/pages/skill-detail/skill-detail?id=${this.data.skillId}`,
    };
  },

  // ===== 数据加载 =====

  async loadDetail() {
    try {
      const res = await shelfService.getDetail(this.data.skillId);
      const skill = res.data;
      const trust = getTrustLevel(skill.trustScore || 0);
      const trustTheme = trust.level === '金牌' ? 'success' : trust.level === '靠谱' ? 'primary' : 'default';
      const locType = skill.locationType;
      const locationIcon = locType === 1 ? 'laptop' : locType === 2 ? 'location' : 'check-circle';
      const locationTheme = locType === 1 ? 'warning' : locType === 2 ? 'danger' : 'success';
      const locationLabel = locType === 1 ? '线上' : locType === 2 ? '线下' : '均可';
      const isOwner = skill.userId === getCurrentUserId();
      let hasOrdered = false;
      try {
        const orderRes = await orderService.getMyOrders({ pageNum: 1, pageSize: 20 });
        hasOrdered = orderRes.data.list.some(
          (o) => o.shelfId === skill.id && o.status !== 0
        );
      } catch (e) {
        console.error('查询订单状态失败:', e);
      }
      // 加载可用时段
      let availableSlots: TimeSlot[] = [];
      let selectedSlotId = 0;
      try {
        const slotsRes = await shelfService.getTimeSlots(this.data.skillId);
        availableSlots = (slotsRes.data || [])
          .filter((s: TimeSlot) => s.available !== false)
          .sort((a: TimeSlot, b: TimeSlot) => `${a.date || ''} ${a.startTime}`.localeCompare(`${b.date || ''} ${b.startTime}`));
        selectedSlotId = availableSlots[0]?.id || 0;
      } catch (e) {
        console.error('加载时段失败:', e);
      }
      this.setData({ skill, isOwner, hasOrdered, trustTheme, trustLabel: trust.label, locationIcon, locationTheme, locationLabel, availableSlots, selectedSlotId, loading: false, loadError: false });
    } catch (err) {
      console.error('加载技能详情失败:', err);
      this.setData({ loading: false, loadError: true });
    }
  },

  // ===== 图片预览 =====

  onPreviewImage(e: WechatMiniprogram.CustomEvent) {
    const { index } = e.currentTarget.dataset;
    const { skill } = this.data;
    if (!skill) return;
    const images = skill.images || [];
    if (!images.length) return;
    wx.previewImage({
      urls: images,
      current: images[index as number] || images[0],
    });
  },

  // ===== 下单 =====

  onShowOrderDialog() {
    if (this.data.isOwner) {
      wx.showToast({ title: '不能购买自己的服务', icon: 'error' });
      return;
    }
    if (this.data.availableSlots.length === 0) {
      wx.showToast({ title: '暂无可预约时间', icon: 'none' });
      return;
    }
    this.setData({ showOrderDialog: true, orderRemark: '' });
  },

  onCloseOrderDialog() {
    this.setData({ showOrderDialog: false });
  },

  onSlotTap(e: WechatMiniprogram.CustomEvent) {
    this.setData({ selectedSlotId: Number(e.currentTarget.dataset.id) });
  },

  onRemarkInput(e: WechatMiniprogram.Input) {
    this.setData({ orderRemark: e.detail.value });
  },

  async onConfirmOrder() {
    const { skillId, ordering, isOwner } = this.data;
    if (ordering) return;
    if (isOwner) {
      wx.showToast({ title: '不能购买自己的服务', icon: 'error' });
      return;
    }

    this.setData({ ordering: true });
    let timeoutId = 0;
    try {
      const params: CreateFromShelfParams = {
        shelfId: skillId,
        timeSlotId: this.data.selectedSlotId || undefined,
        remark: this.data.orderRemark || undefined,
      };
      const timeoutPromise = new Promise<never>((_, reject) => {
        timeoutId = setTimeout(() => reject({ code: -1, msg: '下单超时' }), 5000) as unknown as number;
      });
      await Promise.race([orderService.createFromShelf(params), timeoutPromise]);
      wx.showToast({ title: '下单成功', icon: 'success' });
      this.setData({ showOrderDialog: false, hasOrdered: true });
      this.loadDetail();
      (this as any)._navTimer = setTimeout(() => {
        const pages = getCurrentPages();
        if (pages.length > 1) wx.navigateBack();
        else wx.switchTab({ url: '/pages/index/index' });
      }, 1200) as unknown as number;
    } catch (err) {
      console.error('下单失败:', err);
      wx.showToast({ title: '下单失败，请重试', icon: 'error' });
    } finally {
      if (timeoutId) clearTimeout(timeoutId);
      this.setData({ ordering: false });
    }
  },

  // ===== 聊天 =====

  onChat() {
    const { skill } = this.data;
    if (!skill) return;
    wx.navigateTo({
      url: `/pages/chat/chat?targetUserId=${skill.userId}&targetNickname=${encodeURIComponent(skill.nickname || '')}`,
    });
  },

  onUnload() {
    const self = this as any;
    if (self._navTimer) clearTimeout(self._navTimer);
  },
});
