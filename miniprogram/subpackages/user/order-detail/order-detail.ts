/**
 * 订单详情 + 核销页（新流程）
 * 1(待核销) → 双方输入对方核销码 → 3(服务中) → 双方输入对方确认码 → 5(已完成)
 * 服务中可申请退款（需对方同意），争议可在3/5发起
 */

import { orderService } from '../../../services/order';
import type { OrderDetail } from '../../../types/order';
import { ORDER_STATUS_MAP } from '../../../utils/order-status';

function getCurrentUserId(): number {
  try {
    const app = getApp();
    const storedUser = wx.getStorageSync('dabashou_user');
    const user = app.globalData.userInfo || (typeof storedUser === 'string' ? JSON.parse(storedUser) : storedUser);
    return user?.id || 1001;
  } catch {
    return 1001;
  }
}

Page({
  data: {
    orderId: 0,
    order: null as OrderDetail | null,
    ORDER_STATUS_MAP,
    loading: true,
    loadError: false,
    statusTheme: 'default' as string,
    // ── 核销码弹窗 ──
    showCodeDialog: false,
    codeDialogTitle: '',
    codeInput: '',
    codeAction: '' as '' | 'start' | 'complete',
    // ── 取消弹窗 ──
    showCancelDialog: false,
    cancelReason: '',
    // ── 争议弹窗 ──
    showDisputeDialog: false,
    disputeReason: '',
    // ── 操作 loading ──
    actionLoading: false,
  },

  onLoad(options: Record<string, string | undefined>) {
    const id = Number(options.id);
    if (!id) { wx.showToast({ title: '参数错误', icon: 'error' }); const pages = getCurrentPages(); if (pages.length > 1) wx.navigateBack(); else wx.switchTab({ url: '/pages/index/index' }); return; }
    this.setData({ orderId: id });
    this.loadDetail();
  },

  onShow() {
    if (this.data.orderId && this.data.order) this.loadDetail();
  },

  async loadDetail() {
    try {
      const res = await orderService.getDetail(this.data.orderId);
      const order = res.data;
      const status = order.status;
      const statusTheme =
        status === 0 ? 'default' : status === 1 ? 'warning' :
        status === 3 ? 'primary' : status === 5 ? 'success' :
        status === 7 ? 'danger' : 'default';
      const myId = getCurrentUserId();
      const myRole: 'buyer' | 'seller' = order.buyerId === myId ? 'buyer' : 'seller';
      this.setData({ order: { ...order, myRole }, statusTheme, loading: false, loadError: false });
    } catch (err) {
      console.error('加载订单详情失败:', err);
      this.setData({ loading: false, loadError: true });
    }
  },

  // =====================================================================
  //                    核销码弹窗（start / complete）
  // =====================================================================
  onCodeBtnTap(e: WechatMiniprogram.BaseEvent) {
    const { title, action } = e.currentTarget.dataset as { title: string; action: string };
    this.setData({ showCodeDialog: true, codeDialogTitle: title || '', codeInput: '', codeAction: (action || '') as '' | 'start' | 'complete' });
  },
  onCloseCodeDialog() { this.setData({ showCodeDialog: false, codeInput: '' }); },
  onCodeInput(e: WechatMiniprogram.Input) { this.setData({ codeInput: e.detail.value }); },

  async onConfirmCode() {
    const { orderId, codeInput, codeAction, actionLoading } = this.data;
    if (actionLoading || !codeAction) return;
    const code = codeInput.trim();
    if (!code) { wx.showToast({ title: '请输入核销码', icon: 'error' }); return; }
    this.setData({ actionLoading: true });
    try {
      await orderService.verify(orderId, code, codeAction);
      wx.showToast({ title: codeAction === 'start' ? '启动核销成功' : '确认核销成功', icon: 'success' });
      this.setData({ showCodeDialog: false, codeInput: '' });
      this.loadDetail();
    } catch (err: unknown) {
      wx.showToast({ title: (err as Record<string, string>)?.msg || '核销码错误', icon: 'error' });
    } finally { this.setData({ actionLoading: false }); }
  },

  // =====================================================================
  //                     退款（双向同意）
  // =====================================================================
  onShowRefundDialog() {
    const { order } = this.data;
    const role = order?.myRole || 'buyer';
    const page = this;
    if (order?.refundRequesting) {
      const requester = order.refundRequester === role ? '你' : (order.refundRequester === 'buyer' ? '买家' : '卖家');
      wx.showModal({
        title: '退款确认',
        content: `${requester}发起退款申请，是否同意？`,
        success: (res) => { if (res.confirm) page.onConfirmRefund(); },
      });
    } else {
      wx.showModal({
        title: '发起退款',
        content: '发起后将等待对方同意，确定继续？',
        success: (res) => { if (res.confirm) page.onConfirmRefund(); },
      });
    }
  },

  async onConfirmRefund() {
    const { orderId, actionLoading } = this.data;
    if (actionLoading) return;
    this.setData({ actionLoading: true });
    try {
      await orderService.refundOrder(orderId);
      wx.showToast({ title: '退款操作成功', icon: 'success' });
      this.loadDetail();
    } catch (err) {
      wx.showToast({ title: '操作失败', icon: 'error' });
    } finally { this.setData({ actionLoading: false }); }
  },

  // =====================================================================
  //                     争议
  // =====================================================================
  onShowDisputeDialog() { this.setData({ showDisputeDialog: true, disputeReason: '' }); },
  onCloseDisputeDialog() { this.setData({ showDisputeDialog: false }); },
  onDisputeReasonInput(e: WechatMiniprogram.Input) { this.setData({ disputeReason: e.detail.value }); },

  async onConfirmDispute() {
    const { orderId, disputeReason, actionLoading } = this.data;
    if (actionLoading) return;
    this.setData({ actionLoading: true });
    try {
      await orderService.disputeOrder(orderId, disputeReason || undefined);
      wx.showToast({ title: '争议已提交', icon: 'success' });
      this.setData({ showDisputeDialog: false });
      this.loadDetail();
    } catch (err) {
      wx.showToast({ title: '操作失败', icon: 'error' });
    } finally { this.setData({ actionLoading: false }); }
  },

  // =====================================================================
  //                     取消订单
  // =====================================================================
  onShowCancelDialog() { this.setData({ showCancelDialog: true, cancelReason: '' }); },
  onCloseCancelDialog() { this.setData({ showCancelDialog: false }); },
  onCancelReasonInput(e: WechatMiniprogram.Input) { this.setData({ cancelReason: e.detail.value }); },

  async onConfirmCancel() {
    const { orderId, cancelReason, actionLoading } = this.data;
    if (actionLoading) return;
    this.setData({ actionLoading: true });
    try {
      await orderService.cancel(orderId, cancelReason || undefined);
      wx.showToast({ title: '订单已取消', icon: 'success' });
      this.setData({ showCancelDialog: false });
      (this as any)._navTimer = setTimeout(() => {
        const pages = getCurrentPages();
        if (pages.length > 1) wx.navigateBack();
        else wx.switchTab({ url: '/pages/index/index' });
      }, 1200) as unknown as number;
    } catch (err) {
      console.error('取消订单失败:', err);
      wx.showToast({ title: '取消失败', icon: 'error' });
    } finally { this.setData({ actionLoading: false }); }
  },

  // =====================================================================
  //                       辅助
  // =====================================================================
  onUnload() {
    const self = this as any;
    if (self._navTimer) clearTimeout(self._navTimer);
  },
});
