/**
 * 技能货架管理页
 * 查看/上下架/编辑自己发布的技能
 */

import { shelfService } from '../../../services/shelf';
import type { SkillShelf, ShelfStatus } from '../../../types/shelf';

/** 货架状态中文映射 */
const SHELF_STATUS_MAP: Record<ShelfStatus, string> = {
  0: '已下架',
  1: '上架中',
  2: '审核中',
};

/** 货架状态标签主题色 */
const SHELF_STATUS_THEME: Record<ShelfStatus, string> = {
  0: 'default',   // gray
  1: 'success',   // green
  2: 'warning',   // orange
};

Page({
  data: {
    /** 我的技能列表 */
    shelfList: [] as SkillShelf[],
    /** 状态映射 */
    SHELF_STATUS_MAP,
    SHELF_STATUS_THEME,
    /** 加载中 */
    loading: true,
    /** 分页 */
    pageNum: 1,
    pageSize: 20,
    hasMore: true,
    /** 上下架确认弹窗 */
    showToggleDialog: false,
    /** 弹窗目标技能ID */
    toggleTargetId: 0,
    /** 弹窗目标状态 */
    toggleTargetStatus: 0 as ShelfStatus,
    /** 操作 loading */
    actionLoading: false,
  },

  onLoad() {
    this.loadMyShelf();
  },

  onShow() {
    this.setData({ pageNum: 1, hasMore: true });
    this.loadMyShelf();
  },

  // 上拉加载更多
  onReachBottom() {
    if (!this.data.hasMore) return;
    this.setData({ pageNum: this.data.pageNum + 1 });
    this.loadMyShelf(true);
  },

  // ===== 数据加载 =====

  async loadMyShelf(append = false) {
    try {
      const { pageNum, pageSize } = this.data;
      const res = await shelfService.getMine({ pageNum, pageSize });
      const newList = append ? [...this.data.shelfList, ...res.data.list] : res.data.list;
      this.setData({
        shelfList: newList,
        hasMore: newList.length < res.data.total,
        loading: false,
      });
    } catch (err) {
      console.error('加载我的技能失败:', err);
      this.setData({ loading: false });
    }
  },

  // ===== 跳转发布 =====

  goPublish() {
    wx.navigateTo({ url: '/pages/publish/publish' });
  },

  // ===== 编辑技能 =====

  onEdit(e: WechatMiniprogram.CustomEvent) {
    const { id } = e.currentTarget.dataset;
    wx.navigateTo({ url: `/pages/publish/publish?editId=${id}` });
  },

  // ===== 上下架 =====

  onToggleStatus(e: WechatMiniprogram.CustomEvent) {
    const { id, status } = e.currentTarget.dataset;
    const newStatus: ShelfStatus = status === 1 ? 0 : 1;
    this.setData({
      showToggleDialog: true,
      toggleTargetId: id,
      toggleTargetStatus: newStatus,
    });
  },

  onCloseToggleDialog() {
    this.setData({ showToggleDialog: false });
  },

  async onConfirmToggle() {
    const { toggleTargetId, toggleTargetStatus, actionLoading } = this.data;
    if (actionLoading) return;

    this.setData({ actionLoading: true });
    try {
      if (toggleTargetStatus === 1) {
        await shelfService.onShelf(toggleTargetId);
      } else {
        await shelfService.offShelf(toggleTargetId);
      }
      wx.showToast({
        title: toggleTargetStatus === 1 ? '已上架' : '已下架',
        icon: 'success',
      });
      this.setData({ showToggleDialog: false });
      this.setData({ pageNum: 1, hasMore: true });
      this.loadMyShelf();
    } catch (err) {
      console.error('上下架失败:', err);
      wx.showToast({ title: '操作失败', icon: 'error' });
    } finally {
      this.setData({ actionLoading: false });
    }
  },
});
