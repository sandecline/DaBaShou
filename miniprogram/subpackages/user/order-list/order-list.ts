/**
 * 订单列表页
 * Tabs: 全部/进行中/已完成，下拉刷新 + 上拉加载更多
 */

import { orderService } from '../../../services/order';
import type { Order, OrderStatus } from '../../../types/order';
import { ORDER_STATUS_MAP } from '../../../utils/order-status';

/** Tab 对应的状态筛选 */
const TAB_FILTER: Record<number, OrderStatus | undefined> = {
  0: undefined,       // 全部
  1: 3 as OrderStatus, // 进行中（服务中）
  2: 5 as OrderStatus, // 已完成
};

/** 状态对应的 t-tag theme（#120 修复：预计算避免 WXML 嵌套三元） */
const STATUS_THEME_MAP: Record<number, string> = {
  0: 'default',
  1: 'warning',
  2: 'warning',
  3: 'primary',
  4: 'primary',
  5: 'success',
  6: 'default',
  7: 'danger',
};

Page({
  data: {
    /** 当前 Tab 索引 */
    activeTab: 0,
    /** 订单列表 */
    orderList: [] as Order[],
    /** 状态映射 */
    ORDER_STATUS_MAP,
    /** 加载中 */
    loading: true,
    /** 分页 */
    pageNum: 1,
    pageSize: 10,
    hasMore: true,
  },

  onLoad() {
    this.loadOrderList();
  },

  onShow() {
    // 从详情页返回时刷新列表
    this.setData({ pageNum: 1, hasMore: true });
    this.loadOrderList();
  },

  // 下拉刷新
  async onPullDownRefresh() {
    this.setData({ pageNum: 1, hasMore: true });
    await this.loadOrderList();
    wx.stopPullDownRefresh();
  },

  // 上拉加载更多
  onReachBottom() {
    if (!this.data.hasMore) return;
    this.setData({ pageNum: this.data.pageNum + 1 });
    this.loadOrderList(true);
  },

  // Tab 切换
  onTabChange(e: WechatMiniprogram.CustomEvent) {
    const index = e.detail.value ?? e.detail.index ?? 0;
    this.setData({ activeTab: index, pageNum: 1, hasMore: true, orderList: [] });
    this.loadOrderList();
  },

  // 点击订单卡片 → 跳转详情
  onOrderTap(e: WechatMiniprogram.CustomEvent) {
    const { id } = e.currentTarget.dataset;
    wx.navigateTo({ url: `/subpackages/user/order-detail/order-detail?id=${id}` });
  },

  // ===== 数据加载 =====

  async loadOrderList(append = false) {
    try {
      const { activeTab, pageNum, pageSize } = this.data;
      const status = TAB_FILTER[activeTab];
      const res = await orderService.getList({
        status,
        pageNum,
        pageSize,
      });
      const newList = (append ? [...this.data.orderList, ...res.data.list] : res.data.list)
        .map((order) => ({ ...order, _statusTheme: STATUS_THEME_MAP[order.status] || 'default' }));
      this.setData({
        orderList: newList,
        hasMore: newList.length < res.data.total,
        loading: false,
      });
    } catch (err) {
      console.error('加载订单列表失败:', err);
      this.setData({ loading: false });
    }
  },
});
