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
    return api.get<PageResult<Order>>('/v1/order', params as unknown as Record<string, unknown>);
  },

  /** 获取我的订单（买家视角） */
  getMyOrders(params: { pageNum: number; pageSize: number }) {
    return api.get<PageResult<Order>>('/v1/order', { ...params, role: 'buyer' } as unknown as Record<string, unknown>);
  },

  /** 获取我接的订单（卖家视角） */
  getMyTakenOrders(params: { pageNum: number; pageSize: number }) {
    return api.get<PageResult<Order>>('/v1/order', { ...params, role: 'seller' } as unknown as Record<string, unknown>);
  },

  /** 获取订单详情（含核销码） */
  getDetail(orderId: number) {
    return api.get<OrderDetail>(`/v1/order/${orderId}`);
  },

  /** 获取订单状态 */
  getOrderStatus(orderId: number) {
    return api.get<{ status: number; statusDesc: string }>(`/v1/order/${orderId}/status`);
  },

  /** 从货架创建订单 */
  createFromShelf(params: CreateFromShelfParams) {
    return api.post<{ orderId: number; orderNo: string }>(
      '/v1/order/from-shelf',
      params as unknown as Record<string, unknown>
    );
  },

  /** 从需求创建订单 */
  createFromDemand(params: CreateFromDemandParams) {
    return api.post<{ orderId: number; orderNo: string }>(
      '/v1/order/from-demand',
      params as unknown as Record<string, unknown>
    );
  },

  /** 支付订单 */
  payOrder(orderId: number) {
    return api.post<void>(`/v1/order/${orderId}/pay`);
  },

  /** 取消订单 */
  cancel(orderId: number, reason?: string) {
    return api.post<void>(`/v1/order/${orderId}/cancel`, { reason });
  },

  /** 开始服务 */
  startService(orderId: number) {
    return api.post<void>(`/v1/order/${orderId}/start`);
  },

  /** 接单 */
  accept(orderId: number) {
    return api.post<{ status: number; statusDesc: string }>(`/v1/order/${orderId}/accept`);
  },

  /** 完成服务（双向验证） */
  completeService(orderId: number, role: 'buyer' | 'seller', code: string) {
    return api.post<{ status: number; statusDesc: string }>(`/v1/order/${orderId}/complete`, { role, code } as unknown as Record<string, unknown>);
  },

  /** 获取核销码 */
  getVerifyCode(orderId: number) {
    return api.get<{ verifyCode: string; expireTime: string }>(`/v1/order/${orderId}/verify-code`);
  },

  /** 刷新核销码 */
  refreshVerifyCode(orderId: number) {
    return api.put<{ verifyCode: string; expireTime: string }>(`/v1/order/${orderId}/verify-code`);
  },

  /** 核销（卖家输入核销码完成服务） */
  verify(orderId: number, verifyCode: string) {
    return api.post<void>(`/v1/order/${orderId}/verify`, { verifyCode });
  },

  /** 确认完成（买家确认服务完成） */
  confirmOrder(orderId: number) {
    return api.post<void>(`/v1/order/${orderId}/confirm`);
  },

  /** 发起争议 */
  disputeOrder(orderId: number, reason?: string) {
    return api.post<void>(`/v1/order/${orderId}/dispute`, { reason });
  },

  /** 退款 */
  refundOrder(orderId: number, reason?: string) {
    return api.post<void>(`/v1/order/${orderId}/refund`, { reason });
  },

  /** 获取订单关联的评价 */
  getOrderReview(orderId: number) {
    return api.get<Record<string, unknown>>(`/v1/orders/${orderId}/review`);
  },
};
