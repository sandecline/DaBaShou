/**
 * 技能详情页
 * 查看技能信息、下单、联系卖主
 */

import { shelfService } from '../../services/shelf';
import { orderService } from '../../services/order';
import type { SkillShelf } from '../../types/shelf';
import type { CreateFromShelfParams } from '../../services/order';
import { getTrustLevel } from '../../utils/enums';

function getCurrentUserId(): number {
  try {
    const app = getApp();
    const user = app.globalData.userInfo || wx.getStorageSync('user_info');
    return user?.id || 1001;
  } catch {
    return 1001;
  }
}

function getCurrentUserId(): number {
  try {
    const app = getApp();
    const user = app.globalData.userInfo || wx.getStorageSync('user_info');
    return user?.id || 1001;
  } catch {
    return 1001;
  }
}

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
  },

  onLoad(options: Record<string, string | undefined>) {
    const id = Number(options.id);
    if (!id) {
      wx.showToast({ title: '参数错误', icon: 'error' });
      wx.navigateBack();
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
      // 预计算 WXML 中使用的派生值，避免嵌套三元表达式
      const trust = getTrustLevel(skill.trustScore || 0);
      const trustTheme = trust.level === '金牌' ? 'success' : trust.level === '靠谱' ? 'primary' : 'default';
      const locType = skill.locationType;
      const locationIcon = locType === 1 ? 'laptop' : locType === 2 ? 'location' : 'check-circle';
      const locationTheme = locType === 1 ? 'warning' : locType === 2 ? 'danger' : 'success';
      const locationLabel = locType === 1 ? '线上' : locType === 2 ? '线下' : '均可';
      const isOwner = skill.userId === getCurrentUserId();
      // 检查当前用户是否已对该技能下单（未取消）
      let hasOrdered = false;
      try {
        const orderRes = await orderService.getMyOrders({ pageNum: 1, pageSize: 100 });
        hasOrdered = orderRes.data.list.some(
          (o) => ((o as unknown as { skillShelfId?: number }).skillShelfId === skill.id) && o.status !== 0
        );
      } catch (e) {
        console.error('查询订单状态失败:', e);
      }
      this.setData({ skill, isOwner, hasOrdered, trustTheme, trustLabel: trust.label, locationIcon, locationTheme, locationLabel, loading: false, loadError: false });
    } catch (err) {
      console.error('加载技能详情失败:', err);
      // #61 修复：设置错误状态，而非保持 skill=null 导致白屏
      this.setData({ loading: false, loadError: true });
    }
  },

  // ===== 图片预览 =====

  onPreviewImage(e: WechatMiniprogram.CustomEvent) {
    const { index } = e.currentTarget.dataset;
    const { skill } = this.data;
    if (!skill) return;
    const images = (skill as any).images || [];
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
    this.setData({ showOrderDialog: true, orderRemark: '' });
  },

  onCloseOrderDialog() {
    this.setData({ showOrderDialog: false });
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
    try {
      const params: CreateFromShelfParams = {
        shelfId: skillId,
        remark: this.data.orderRemark || undefined,
      };
      const timeoutPromise = new Promise<never>((_, reject) => {
        setTimeout(() => reject({ code: -1, msg: '下单超时' }), 5000);
      });
      await Promise.race([orderService.createFromShelf(params), timeoutPromise]);
      wx.showToast({ title: '下单成功', icon: 'success' });
      this.setData({ showOrderDialog: false, hasOrdered: true });
      this.loadDetail();
      setTimeout(() => {
        wx.navigateTo({ url: '/subpackages/user/order-list/order-list' });
      }, 1200);
    } catch (err) {
      console.error('下单失败:', err);
      wx.showToast({ title: '下单失败，请重试', icon: 'error' });
    } finally {
      this.setData({ ordering: false });
    }
  },

  // ===== 聊天 =====

  onChat() {
    const { skill } = this.data;
    if (!skill) return;
    wx.navigateTo({
      url: `/pages/chat/chat?targetUserId=${skill.userId}&targetNickname=${encodeURIComponent((skill as any).nickname || '')}`,
    });
  },
});
