/**
 * 订单详情 + 核销页
 * 核销码复制、确认完成、取消订单
 */

import { orderService } from '../../../services/order';
import type { OrderDetail, OrderStatus } from '../../../types/order';
import { ORDER_STATUS_MAP } from '../../../utils/order-status';

Page({
  data: {
    /** 订单ID */
    orderId: 0,
    /** 订单详情 */
    order: null as OrderDetail | null,
    /** 状态映射 */
    ORDER_STATUS_MAP,
    /** 加载中 */
    loading: true,
    /** 加载是否出错 */
    loadError: false,
    /** 预计算：状态标签主题 */
    statusTheme: 'default' as string,
    /** 取消弹窗 */
    showCancelDialog: false,
    /** 取消原因 */
    cancelReason: '',
    /** 核销弹窗 */
    showVerifyDialog: false,
    /** 核销码输入 */
    verifyCodeInput: '',
    /** 操作 loading */
    actionLoading: false,
  },

  onLoad(options: Record<string, string | undefined>) {
    const id = Number(options.id);
    if (!id) {
      wx.showToast({ title: '参数错误', icon: 'error' });
      wx.navigateBack();
      return;
    }
    this.setData({ orderId: id });
    this.loadDetail();
  },

  /**
   * #121 修复：从其他页面返回时刷新订单状态
   */
  onShow() {
    if (this.data.orderId && this.data.order) {
      this.loadDetail();
    }
  },

  // ===== 数据加载 =====

  async loadDetail() {
    try {
      const res = await orderService.getDetail(this.data.orderId);
      const order = res.data;
      // #124 修复：预计算状态标签主题，避免 WXML 嵌套三元
      const status = order.status;
      const statusTheme =
        status === 0 ? 'default' :
        status === 1 ? 'warning' :
        status === 3 ? 'primary' :
        status === 5 ? 'success' :
        status === 7 ? 'danger' : 'default';
      this.setData({ order, statusTheme, loading: false, loadError: false });
    } catch (err) {
      console.error('加载订单详情失败:', err);
      this.setData({ loading: false, loadError: true });
    }
  },

  // ===== 核销码 =====

  onCopyVerifyCode() {
    const { order } = this.data;
    if (!order?.verifyCode) return;
    wx.setClipboardData({
      data: order.verifyCode,
      success() {
        wx.showToast({ title: '核销码已复制', icon: 'success' });
      },
    });
  },

  // ===== 核销（卖家） =====

  onShowVerifyDialog() {
    this.setData({ showVerifyDialog: true, verifyCodeInput: '' });
  },

  onCloseVerifyDialog() {
    this.setData({ showVerifyDialog: false });
  },

  onVerifyCodeInput(e: WechatMiniprogram.Input) {
    this.setData({ verifyCodeInput: e.detail.value });
  },

  async onConfirmVerify() {
    const { orderId, verifyCodeInput, actionLoading } = this.data;
    if (actionLoading) return;
    if (!verifyCodeInput.trim()) {
      wx.showToast({ title: '请输入核销码', icon: 'none' });
      return;
    }

    this.setData({ actionLoading: true });
    try {
      await orderService.verify(orderId, verifyCodeInput.trim());
      wx.showToast({ title: '核销成功', icon: 'success' });
      this.setData({ showVerifyDialog: false });
      this.loadDetail();
    } catch (err) {
      console.error('核销失败:', err);
      wx.showToast({ title: '核销失败，请重试', icon: 'error' });
    } finally {
      this.setData({ actionLoading: false });
    }
  },

  // ===== 确认完成（买家） =====

  async onConfirmComplete() {
    const { orderId, actionLoading } = this.data;
    if (actionLoading) return;

    this.setData({ actionLoading: true });
    try {
      await orderService.confirmComplete(orderId);
      wx.showToast({ title: '已确认完成', icon: 'success' });
      this.loadDetail();
    } catch (err) {
      console.error('确认完成失败:', err);
      wx.showToast({ title: '操作失败', icon: 'error' });
    } finally {
      this.setData({ actionLoading: false });
    }
  },

  // ===== 取消订单 =====

  onShowCancelDialog() {
    this.setData({ showCancelDialog: true, cancelReason: '' });
  },

  onCloseCancelDialog() {
    this.setData({ showCancelDialog: false });
  },

  onCancelReasonInput(e: WechatMiniprogram.Input) {
    this.setData({ cancelReason: e.detail.value });
  },

  async onConfirmCancel() {
    const { orderId, cancelReason, actionLoading } = this.data;
    if (actionLoading) return;

    this.setData({ actionLoading: true });
    try {
      await orderService.cancel(orderId, cancelReason || undefined);
      wx.showToast({ title: '订单已取消', icon: 'success' });
      this.setData({ showCancelDialog: false });
      this.loadDetail();
    } catch (err) {
      console.error('取消订单失败:', err);
      wx.showToast({ title: '取消失败', icon: 'error' });
    } finally {
      this.setData({ actionLoading: false });
    }
  },

  // ===== 辅助方法 =====

  /** 是否可取消：待支付(1)、已支付(2) 状态 */
  canCancel(): boolean {
    const { order } = this.data;
    return order ? (order.status === 1 || order.status === 2) : false;
  },

  /** 是否可核销：卖家 + 服务中(3) */
  canVerify(): boolean {
    const { order } = this.data;
    return order ? (order.myRole === 'seller' && order.status === 3) : false;
  },

  /** 是否可确认完成：买家 + 待确认(4) */
  canConfirmComplete(): boolean {
    const { order } = this.data;
    return order ? (order.myRole === 'buyer' && order.status === 4) : false;
  },
});
