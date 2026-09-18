/**
 * 求助管理页
 * 查看/管理自己发布的求助
 */

import { demandService } from '../../../services/demand';
import type { Demand } from '../../../types/demand';
import { ensureLogin } from '../../../utils/auth';

/** 需求状态映射（与 DemandStatus 类型 0|1|2|3 保持一致） */
const DEMAND_STATUS_MAP: Record<number, string> = {
  0: '已关闭',
  1: '待接单',
  2: '进行中',
  3: '已完成',
};

/** 状态主题色 */
const DEMAND_STATUS_THEME: Record<number, string> = {
  0: 'default',
  1: 'warning',
  2: 'primary',
  3: 'success',
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

  async onLoad() {
    const loggedIn = await ensureLogin();
    if (!loggedIn) return;
    this.loadMyDemands();
  },

  onShow() {
    // 从详情页返回时刷新列表
    this.setData({ hasMore: true });
    this.loadMyDemands();
  },

  // 上拉加载更多
  onReachBottom() {
    if (!this.data.hasMore) return;
    this.loadMyDemands(true);
  },

  // ===== 数据加载 =====

  async loadMyDemands(append = false) {
    try {
      const { pageSize } = this.data;
      const pageNum = append ? this.data.pageNum : 1;
      const res = await demandService.getMine({ pageNum, pageSize });
      const newList = append ? [...this.data.demandList, ...res.data.list] : res.data.list;
      this.setData({
        demandList: newList,
        pageNum: pageNum + 1,
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
      this.setData({ showCancelDialog: false, hasMore: true });
      this.loadMyDemands();
    } catch (err) {
      console.error('取消求助失败:', err);
      wx.showToast({ title: '操作失败', icon: 'error' });
    } finally {
      this.setData({ actionLoading: false });
    }
  },
});
