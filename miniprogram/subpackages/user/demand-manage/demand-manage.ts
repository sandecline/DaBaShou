/**
 * 求助管理页
 * 查看/管理自己发布的求助
 */

import { demandService } from '../../../services/demand';
import type { Demand } from '../../../types/demand';

/** 需求状态映射 */
const DEMAND_STATUS_MAP: Record<number, string> = {
  0: '已取消',
  1: '待接单',
  2: '已接单',
  3: '进行中',
  4: '已完成',
};

/** 状态主题色 */
const DEMAND_STATUS_THEME: Record<number, string> = {
  0: 'default',
  1: 'warning',
  2: 'primary',
  3: 'primary',
  4: 'success',
};

Page({
  data: {
    /** 我的求助列表 */
    demandList: [] as Demand[],
    /** 状态映射 */
    DEMAND_STATUS_MAP,
    DEMAND_STATUS_THEME,
    /** 加载中 */
    loading: true,
    /** 分页 */
    pageNum: 1,
    pageSize: 20,
    hasMore: true,
    /** 取消确认弹窗 */
    showCancelDialog: false,
    cancelTargetId: 0,
    actionLoading: false,
  },

  onLoad() {
    this.loadMyDemands();
  },

  onShow() {
    this.setData({ pageNum: 1, hasMore: true });
    this.loadMyDemands();
  },

  onReachBottom() {
    if (!this.data.hasMore) return;
    this.setData({ pageNum: this.data.pageNum + 1 });
    this.loadMyDemands(true);
  },

  // ===== 数据加载 =====

  async loadMyDemands(append = false) {
    try {
      const { pageNum, pageSize } = this.data;
      const res = await demandService.getMine({ pageNum, pageSize });
      const newList = append ? [...this.data.demandList, ...res.data.list] : res.data.list;
      this.setData({
        demandList: newList,
        hasMore: newList.length < res.data.total,
        loading: false,
      });
    } catch (err) {
      console.error('加载我的求助失败:', err);
      this.setData({ loading: false });
    }
  },

  // ===== 跳转发布 =====

  goPublish() {
    wx.navigateTo({ url: '/pages/publish-demand/publish-demand' });
  },

  // ===== 点击详情 =====

  onDemandTap(e: WechatMiniprogram.CustomEvent) {
    const { id } = e.currentTarget.dataset;
    wx.navigateTo({ url: `/pages/demand-detail/demand-detail?id=${id}` });
  },

  // ===== 取消求助 =====

  onCancelDemand(e: WechatMiniprogram.CustomEvent) {
    const { id } = e.currentTarget.dataset;
    this.setData({ showCancelDialog: true, cancelTargetId: id });
  },

  onCloseCancelDialog() {
    this.setData({ showCancelDialog: false });
  },

  async onConfirmCancel() {
    const { cancelTargetId, actionLoading } = this.data;
    if (actionLoading) return;

    this.setData({ actionLoading: true });
    try {
      await demandService.close(cancelTargetId);
      wx.showToast({ title: '已取消', icon: 'success' });
      this.setData({ showCancelDialog: false, pageNum: 1, hasMore: true });
      this.loadMyDemands();
    } catch (err) {
      console.error('取消求助失败:', err);
      wx.showToast({ title: '操作失败', icon: 'error' });
    } finally {
      this.setData({ actionLoading: false });
    }
  },
});
