/**
 * PaymentService 接口定义 — 当前虚拟积分实现，预留微信支付扩展
 *
 * 使用方式：
 *   import { paymentService } from '@/services/payment';
 *   await paymentService.freezePoints(userId, amount, orderId);
 */

import { api } from '../utils/request';

// ===== 接口定义 =====

export interface IPaymentService {
  /** 冻结积分（创建订单时调用） */
  freezePoints(userId: number, amount: number, orderId: number): Promise<void>;

  /** 解冻并结算积分（订单完成时调用） */
  settlePoints(orderId: number): Promise<void>;

  /** 退还积分（订单取消/超时时调用，可选罚金比例） */
  refundPoints(orderId: number, penaltyRate?: number): Promise<void>;

  /** 获取积分余额 */
  getBalance(userId: number): Promise<{ available: number; frozen: number }>;
}

// ===== 当前实现：虚拟积分 =====

export class VirtualPaymentService implements IPaymentService {
  async freezePoints(userId: number, amount: number, orderId: number): Promise<void> {
    await api.post('/v1/points/freeze', { userId, amount, orderId });
  }

  async settlePoints(orderId: number): Promise<void> {
    await api.put(`/v1/points/settle/${orderId}`);
  }

  async refundPoints(orderId: number, penaltyRate = 0): Promise<void> {
    await api.put(`/v1/points/refund/${orderId}`, { penaltyRate });
  }

  async getBalance(_userId: number): Promise<{ available: number; frozen: number }> {
    const res = await api.get<{ available: number; frozen: number }>('/v1/points/balance');
    return res.data;
  }
}

// TODO: 未来扩展微信支付实现
// export class WechatPaymentService implements IPaymentService {
//   async freezePoints(userId: number, amount: number, orderId: number) {
//     // 调用 wx.requestPayment 发起支付
//   }
//   async settlePoints(orderId: number) {
//     // 支付回调已自动结算，无需额外操作
//   }
//   async refundPoints(orderId: number, penaltyRate?: number) {
//     // 调用微信支付退款接口
//   }
//   ...
// }

// ===== 统一导出 =====

export const paymentService: IPaymentService = new VirtualPaymentService();
