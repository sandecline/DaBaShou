/**
 * 订单详情 + 核销页
 * 接单 → 双向验证启动 → 服务进行 → 双向验证完成 → 积分结算
 * 服务期间双方可发起退款，需对方同意
 */

import { orderService } from '../../../services/order';
import type { OrderDetail } from '../../../types/order';
import { ORDER_STATUS_MAP } from '../../../utils/order-status';

Page({
  data: {
    orderId: 0,
    order: null as OrderDetail | null,
    ORDER_STATUS_MAP,
    loading: true,
    loadError: false,
    statusTheme: 'default' as string,
    // ── 接单弹窗 ──
    showAcceptDialog: false,
    acceptCode: '',
    // ── 取消弹窗 ──
    showCancelDialog: false,
    cancelReason: '',
    // ── 通用验证码弹窗 ──
    showCodeDialog: false,
    codeDialogTitle: '',
    codeInput: '',
    codeAction: '' as '' | 'completeBuyer' | 'completeSeller' | 'refund',
    // ── 退款确认弹窗 ──
    showRefundDialog: false,
    // ── 操作 loading ──
    actionLoading: false,
  },

  onLoad(options: Record<string, string | undefined>) {
    const id = Number(options.id);
    if (!id) { wx.showToast({ title: '参数错误', icon: 'error' }); wx.navigateBack(); return; }
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
      this.setData({ order, statusTheme, loading: false, loadError: false });
    } catch (err) {
      console.error('加载订单详情失败:', err);
      this.setData({ loading: false, loadError: true });
    }
  },

  // =====================================================================
  //                           接单（卖家）
  // =====================================================================
  onShowAcceptDialog() { this.setData({ showAcceptDialog: true }); },
  onCloseAcceptDialog() { this.setData({ showAcceptDialog: false }); },

  async onConfirmAccept() {
    const { orderId, actionLoading } = this.data;
    if (actionLoading) return;
    this.setData({ actionLoading: true });
    try {
      await orderService.accept(orderId);
      wx.showToast({ title: '已接单', icon: 'success' });
      this.setData({ showAcceptDialog: false });
      this.loadDetail();
    } catch (err) {
      console.error('接单失败:', err);
      wx.showToast({ title: '操作失败', icon: 'error' });
    } finally { this.setData({ actionLoading: false }); }
  },

  // =====================================================================
  //                    通用验证码弹窗
  // =====================================================================
  openCodeDialog(title: string, action: string) {
    this.setData({ showCodeDialog: true, codeDialogTitle: title, codeInput: '', codeAction: action as '' | 'completeBuyer' | 'completeSeller' });
  },
  onCloseCodeDialog() { this.setData({ showCodeDialog: false, codeInput: '' }); },
  onCodeInput(e: WechatMiniprogram.Input) { this.setData({ codeInput: e.detail.value }); },

  async onConfirmCode() {
    const { orderId, codeInput, codeAction, actionLoading, order } = this.data;
    if (actionLoading || !codeAction || !order) return;
    this.setData({ actionLoading: true });
    try {
      if (codeAction === 'completeBuyer') {
        await orderService.completeService(orderId, 'buyer', codeInput.trim());
        wx.showToast({ title: '买家验证通过', icon: 'success' });
      } else if (codeAction === 'completeSeller') {
        await orderService.completeService(orderId, 'seller', codeInput.trim());
        wx.showToast({ title: '卖家验证通过', icon: 'success' });
      }
      this.setData({ showCodeDialog: false, codeInput: '' });
      this.loadDetail();
    } catch (err: unknown) {
      wx.showToast({ title: (err as Record<string,string>)?.msg || '验证码错误', icon: 'error' });
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
        success: (res) => { if (res.confirm) page.onConfirmRefundApprove(); },
      });
    } else {
      wx.showModal({
        title: '发起退款',
        content: '发起后将等待对方同意，确定继续？',
        success: (res) => { if (res.confirm) page.onConfirmRefundRequest(); },
      });
    }
  },

  async onConfirmRefundRequest() {
    const { orderId, actionLoading, order } = this.data;
    if (actionLoading || !order) return;
    this.setData({ actionLoading: true });
    try {
      await (orderService as unknown as Record<string, (id: number, role: string) => Promise<unknown>>).refundRequest(orderId, order.myRole || 'buyer');
      wx.showToast({ title: '退款申请已提交', icon: 'success' });
      this.loadDetail();
    } catch (err) {
      wx.showToast({ title: '操作失败', icon: 'error' });
    } finally { this.setData({ actionLoading: false }); }
  },

  async onConfirmRefundApprove() {
    const { orderId, actionLoading } = this.data;
    if (actionLoading) return;
    this.setData({ actionLoading: true });
    try {
      await (orderService as unknown as Record<string, (id: number) => Promise<unknown>>).refundApprove(orderId);
      wx.showToast({ title: '退款已同意', icon: 'success' });
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
      setTimeout(() => wx.navigateBack(), 1200);
    } catch (err) {
      console.error('取消订单失败:', err);
      wx.showToast({ title: '取消失败', icon: 'error' });
    } finally { this.setData({ actionLoading: false }); }
  },

  // =====================================================================
  //                       辅助判断
  // =====================================================================
  canCancel(): boolean {
    const s = this.data.order?.status;
    return s !== undefined ? [1, 2, 3, 4].includes(s) : false;
  },
});
