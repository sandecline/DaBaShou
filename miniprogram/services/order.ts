import { api } from '../utils/request';
import type { PageResult } from '../types/api-response';
import type { OrderItemVo, OrderDetailVo, CreateFromShelfParams, CreateFromDemandParams } from '../types/order';

export interface OrderSearchParams {
  role?: 'buyer' | 'seller';
  status?: number | number[];
  pageNum: number;
  pageSize: number;
}

export const orderService = {
  getList(params: OrderSearchParams) {
    const queryParams: Record<string, unknown> = {};
    if (params.role) queryParams.role = params.role;
    if (params.pageNum) queryParams.pageNum = params.pageNum;
    if (params.pageSize) queryParams.pageSize = params.pageSize;
    if (params.status !== undefined) {
      if (Array.isArray(params.status)) {
        queryParams.status = params.status.join(',');
      } else {
        queryParams.status = params.status;
      }
    }
    return api.get<PageResult<OrderItemVo>>('/v1/orders', queryParams);
  },

  getMyOrders(params: { pageNum: number; pageSize: number }) {
    return api.get<PageResult<OrderItemVo>>('/v1/orders', { ...params, role: 'buyer' } as unknown as Record<string, unknown>);
  },

  getMyTakenOrders(params: { pageNum: number; pageSize: number }) {
    return api.get<PageResult<OrderItemVo>>('/v1/orders', { ...params, role: 'seller' } as unknown as Record<string, unknown>);
  },

  getDetail(orderId: number) {
    return api.get<OrderDetailVo>(`/v1/orders/${orderId}`);
  },

  getOrderStatus(orderId: number) {
    return api.get<{ status: number; statusName: string }>(`/v1/orders/${orderId}/status`);
  },

  createFromShelf(params: CreateFromShelfParams) {
    return api.post<number>(
      '/v1/orders/from-shelf',
      { shelfId: params.shelfId, timeSlotId: params.timeSlotId, remark: params.remark, idempotentToken: `wx_${Date.now()}_${Math.random().toString(36).slice(2, 10)}` } as unknown as Record<string, unknown>
    );
  },

  createFromDemand(params: CreateFromDemandParams) {
    const body: Record<string, unknown> = {
      demandId: params.demandId,
      shelfId: params.shelfId,
      remark: params.remark,
      idempotentToken: `wx_${Date.now()}_${Math.random().toString(36).slice(2, 10)}`,
    };
    return api.post<number>(
      '/v1/orders/from-demand',
      body
    );
  },

  cancel(orderId: number, reason?: string) {
    return api.post<void>(`/v1/orders/${orderId}/cancel`, { reason });
  },

  verify(orderId: number, code: string, phase: 'start' | 'complete' = 'start') {
    return api.post<void>(`/v1/orders/${orderId}/verify`, { code, phase });
  },

  getVerifyCode(orderId: number) {
    return api.get<{ verifyCode: string; expireTime: string }>(`/v1/orders/${orderId}/verify-code`);
  },

  refreshVerifyCode(orderId: number) {
    return api.put<{ verifyCode: string; expireTime: string }>(`/v1/orders/${orderId}/verify-code`);
  },

  disputeOrder(orderId: number, reason?: string) {
    return api.post<void>(`/v1/orders/${orderId}/dispute`, { reason });
  },

  refundOrder(orderId: number, reason: string) {
    return api.post<void>(`/v1/orders/${orderId}/refund`, { reason });
  },

  getOrderReview(orderId: number) {
    return api.get<Record<string, unknown>>(`/v1/orders/${orderId}/review`);
  },
};
