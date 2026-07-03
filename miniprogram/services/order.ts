/**
 * 订单服务
 * 对应后端 dabashou-order 模块
 * 与 frontend/src/api/order.ts 对齐
 */

import { api } from '../utils/request';
import type { PageResult } from '../types/api-response';
import type { Order, OrderDetail } from '../types/order';

export interface OrderSearchParams {
  role?: 'buyer' | 'seller';
  status?: number;
  pageNum: number;
  pageSize: number;
}

export interface CreateFromShelfParams {
  shelfId: number;
  timeSlotId?: number;
  remark?: string;
}

export interface CreateFromDemandParams {
  demandId: number;
  sellerId?: number;
  remark?: string;
}

export const orderService = {
  /** 获取订单列表 */
  getList(params: OrderSearchParams) {
    return api.get<PageResult<Order>>('/v1/orders', params as unknown as Record<string, unknown>);
  },

  /** 获取我的订单（买家视角） */
  getMyOrders(params: { pageNum: number; pageSize: number }) {
    return api.get<PageResult<Order>>('/v1/orders', { ...params, role: 'buyer' } as unknown as Record<string, unknown>);
  },

  /** 获取我接的订单（卖家视角） */
  getMyTakenOrders(params: { pageNum: number; pageSize: number }) {
    return api.get<PageResult<Order>>('/v1/orders', { ...params, role: 'seller' } as unknown as Record<string, unknown>);
  },

  /** 获取订单详情（含核销码） */
  getDetail(orderId: number) {
    return api.get<OrderDetail>(`/v1/orders/${orderId}`);
  },

  /** 获取订单状态 */
  getOrderStatus(orderId: number) {
    return api.get<{ status: number; statusDesc: string }>(`/v1/orders/${orderId}/status`);
  },

  /** 从货架创建订单 */
  createFromShelf(params: CreateFromShelfParams) {
    return api.post<{ orderId: number; orderNo: string }>(
      '/v1/orders/from-shelf',
      { skillShelfId: params.shelfId, timeSlotId: params.timeSlotId, remark: params.remark, idempotentToken: `wx_${Date.now()}_${Math.random().toString(36).slice(2, 10)}` } as unknown as Record<string, unknown>
    );
  },

  /** 从需求创建订单 */
  createFromDemand(params: CreateFromDemandParams) {
    return api.post<{ orderId: number; orderNo: string }>(
      '/v1/orders/from-demand',
      { demandId: params.demandId, remark: params.remark, idempotentToken: `wx_${Date.now()}_${Math.random().toString(36).slice(2, 10)}` } as unknown as Record<string, unknown>
    );
  },

  /** 支付订单 */
  payOrder(orderId: number) {
    return api.post<void>(`/v1/orders/${orderId}/pay`);
  },

  /** 取消订单 */
  cancel(orderId: number, reason?: string) {
    return api.post<void>(`/v1/orders/${orderId}/cancel`, { reason });
  },

  /** 核销（双阶段：start=启动服务，complete=确认完成） */
  verify(orderId: number, verifyCode: string, phase: 'start' | 'complete' = 'start') {
    return api.post<void>(`/v1/orders/${orderId}/verify`, { verifyCode, phase });
  },

  /** 发起争议 */
  disputeOrder(orderId: number, reason?: string) {
    return api.post<void>(`/v1/orders/${orderId}/dispute`, { reason });
  },

  /** 退款 */
  refundOrder(orderId: number, reason?: string) {
    return api.post<void>(`/v1/orders/${orderId}/refund`, { reason });
  },

  /** 获取订单关联的评价 */
  getOrderReview(orderId: number) {
    return api.get<Record<string, unknown>>(`/v1/orders/${orderId}/review`);
  },

  /** 获取核销码 */
  getVerifyCode(orderId: number) {
    return api.get<{ verifyCode: string; expireTime: string }>(`/v1/orders/${orderId}/verify-code`);
  },

  /** 刷新核销码 */
  refreshVerifyCode(orderId: number) {
    return api.put<{ verifyCode: string; expireTime: string }>(`/v1/orders/${orderId}/verify-code`);
  },
};
