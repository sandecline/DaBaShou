/**
 * 订单详情 + 核销页（新流程）
 * 1(待核销) → 双方输入对方核销码 → 3(服务中) → 双方输入对方确认码 → 5(已完成)
 * 服务中可申请退款（需对方同意），争议可在3/5发起
 */

import { orderService } from '../../../services/order';
import { creditService } from '../../../services/credit';
import type { OrderDetail } from '../../../types/order';
import { ORDER_STATUS_MAP } from '../../../utils/order-status';
import { formatDate } from '../../../utils/date';
import { ensureLogin, getUserInfo } from '../../../utils/auth';
import { fileService } from '../../../services/file';

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
    // ── 争议弹窗 ──
    showDisputeDialog: false,
    disputeReason: '',
    // ── 评价弹窗 ──
    showReviewDialog: false,
    reviewed: false,
    reviewRating: 5,
    reviewContent: '',
    reviewImages: [] as string[],
    maxReviewImages: 6,
    // ── 操作 loading ──
    actionLoading: false,
  },

  async onLoad(options: Record<string, string | undefined>) {
    const loggedIn = await ensureLogin();
    if (!loggedIn) return;
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
        status === 6 ? 'warning' : status === 7 ? 'danger' : 'default';
      const myId = getUserInfo()?.id || 0;
      let myRole: 'buyer' | 'seller' | null = null;
      if (order.buyerId === myId) myRole = 'buyer';
      else if (order.sellerId === myId) myRole = 'seller';
      // 格式化日期，替换 T 为空格
      const createTimeFormatted = order.createTime ? formatDate(order.createTime, 'YYYY-MM-DD HH:mm:ss') : '';
      // 兼容后端新旧字段：优先使用新字段，回退旧字段
      const displayOrder = {
        ...order,
        myRole,
        createTimeFormatted,
        myStartCode: myRole === 'buyer' ? order.buyerVerifyCode : order.sellerVerifyCode,
        myStartVerified: myRole === 'buyer' ? order.buyerVerified : order.sellerVerified,
        myConfirmCode: myRole === 'buyer' ? order.buyerConfirmCode : order.sellerConfirmCode,
        myConfirmed: myRole === 'buyer' ? order.buyerConfirmed : order.sellerConfirmed,
        refundRequesting: !!order.refundRequester && !order.refundAgreed,
      };
      // 已完成订单查询是否已评价
      let reviewed = false;
      if (displayOrder.status === 5) {
        try {
          const reviewRes = await orderService.getOrderReview(displayOrder.id);
          reviewed = !!(reviewRes.data && (reviewRes.data as { id?: number }).id);
        } catch { reviewed = false; }
      }
      this.setData({ order: displayOrder as OrderDetail, statusTheme, reviewed, loading: false, loadError: false });
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
    const { orderId, codeInput, codeAction, actionLoading, order } = this.data;
    if (actionLoading || !codeAction || !order || !order.myRole) return;
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
  /** 买家/卖家申请退款 */
  onRequestRefund() {
    const { order } = this.data;
    if (!order || !order.myRole) return;
    wx.showModal({
      title: '申请退款',
      content: '发起后将等待对方同意，确定继续？',
      success: async (res) => {
        if (!res.confirm) return;
        this.setData({ actionLoading: true });
        try {
          await orderService.refundOrder(order.id, '申请退款');
          wx.showToast({ title: '退款申请已提交', icon: 'success' });
          this.loadDetail();
        } catch (err) {
          wx.showToast({ title: '操作失败', icon: 'error' });
        } finally { this.setData({ actionLoading: false }); }
      },
    });
  },

  /** 对方同意退款 */
  onApproveRefund() {
    const { order } = this.data;
    if (!order || !order.myRole) return;
    const requester = order.refundRequester === 'buyer' ? '买家' : '卖家';
    wx.showModal({
      title: '同意退款',
      content: `${requester}已发起退款申请，确定同意退款？`,
      success: async (res) => {
        if (!res.confirm) return;
        this.setData({ actionLoading: true });
        try {
          await orderService.refundOrder(order.id, '同意退款');
          wx.showToast({ title: '退款已同意', icon: 'success' });
          this.loadDetail();
        } catch (err) {
          wx.showToast({ title: '操作失败', icon: 'error' });
        } finally { this.setData({ actionLoading: false }); }
      },
    });
  },

  // =====================================================================
  //                     争议
  // =====================================================================
  onShowDisputeDialog() { this.setData({ showDisputeDialog: true, disputeReason: '' }); },
  onCloseDisputeDialog() { this.setData({ showDisputeDialog: false }); },
  onDisputeReasonInput(e: WechatMiniprogram.Input) { this.setData({ disputeReason: e.detail.value }); },

  async onConfirmDispute() {
    const { orderId, disputeReason, actionLoading, order } = this.data;
    if (actionLoading || !order || !order.myRole) return;
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
  onShowCancelDialog() {
    wx.showModal({
      title: '取消订单',
      placeholderText: '请输入取消原因',
      editable: true,
      success: (res) => {
        if (res.confirm) {
          const reason = (res.content || '').trim();
          if (!reason) {
            wx.showToast({ title: '请输入取消原因', icon: 'error' });
            return;
          }
          this.doCancelOrder(reason);
        }
      }
    });
  },

  async doCancelOrder(reason: string) {
    const { orderId, actionLoading, order } = this.data;
    if (actionLoading || !order || !order.myRole) return;
    this.setData({ actionLoading: true });
    try {
      await orderService.cancel(orderId, reason);
      wx.showToast({ title: '订单已取消', icon: 'success' });
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

  onGoToAppeal() {
    wx.navigateTo({ url: `/subpackages/user/credit/appeal/appeal` });
  },

  // =====================================================================
  //                     评价
  // =====================================================================
  onShowReviewDialog() {
    this.setData({ showReviewDialog: true, reviewRating: 5, reviewContent: '', reviewImages: [] });
  },
  onCloseReviewDialog() { this.setData({ showReviewDialog: false }); },
  onRatingChange(e: WechatMiniprogram.PickerChange) {
    const idx = Number(e.detail.value) || 0;
    this.setData({ reviewRating: Math.max(1, Math.min(5, idx + 1)) });
  },
  onReviewInput(e: WechatMiniprogram.Input) { this.setData({ reviewContent: e.detail.value }); },

  async onChooseReviewImage() {
    const { reviewImages, maxReviewImages } = this.data;
    const remaining = maxReviewImages - reviewImages.length;
    if (remaining <= 0) {
      wx.showToast({ title: `最多上传${maxReviewImages}张图片`, icon: 'none' });
      return;
    }
    wx.chooseMedia({
      count: remaining,
      mediaType: ['image'],
      sourceType: ['album', 'camera'],
      success: (res) => {
        const paths = res.tempFiles.map((f) => f.tempFilePath);
        this.setData({ reviewImages: [...reviewImages, ...paths] });
      },
    });
  },

  onRemoveReviewImage(e: WechatMiniprogram.CustomEvent) {
    const { index } = e.currentTarget.dataset;
    const images = [...this.data.reviewImages];
    images.splice(index, 1);
    this.setData({ reviewImages: images });
  },

  async onSubmitReview() {
    const { order, reviewRating, reviewContent, reviewImages, actionLoading } = this.data;
    if (actionLoading || !order || !order.myRole) return;
    if (!reviewContent.trim()) {
      wx.showToast({ title: '请输入评价内容', icon: 'error' });
      return;
    }
    this.setData({ actionLoading: true });
    try {
      let remoteImages: string[] = [];
      if (reviewImages.length > 0) {
        remoteImages = await fileService.uploadBatch(reviewImages);
      }
      await creditService.submitReview({
        orderId: order.id,
        rating: reviewRating,
        content: reviewContent.trim(),
        images: remoteImages,
      });
      wx.showToast({ title: '评价成功', icon: 'success' });
      this.setData({ showReviewDialog: false, reviewed: true });
      this.loadDetail();
    } catch (err: unknown) {
      wx.showToast({ title: (err as Record<string, string>)?.msg || '评价失败', icon: 'error' });
    } finally {
      this.setData({ actionLoading: false });
    }
  },

  // =====================================================================
  //                       辅助
  // =====================================================================
  onUnload() {
    const self = this as any;
    if (self._navTimer) clearTimeout(self._navTimer);
  },
});
