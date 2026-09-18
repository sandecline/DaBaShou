import request from '@/utils/request'
import type { OrderItemVo, OrderDetailVo, VerifyCodeVo, PayResultVo, PageResult, PageParams } from '@/types/api'
import { normalizePageParams } from './_params'

export function createOrderFromShelf(data: { skillShelfId?: number; shelfId?: number; timeSlotId?: number; remark?: string }): Promise<number> {
  const shelfId = data.shelfId ?? data.skillShelfId
  return request.post('/v1/orders/from-shelf', {
    ...data,
    shelfId,
    idempotentToken: crypto.randomUUID(),
  })
}

export function createOrderFromDemand(data: { demandId: number; sellerId?: number; shelfId?: number; remark?: string }): Promise<number> {
  return request.post('/v1/orders/from-demand', {
    ...data,
    idempotentToken: crypto.randomUUID(),
  })
}

export function getOrderList(params: { role?: 'buyer' | 'seller'; status?: number; pageNum?: number; pageSize?: number }): Promise<PageResult<OrderItemVo>> {
  return request.get('/v1/orders', normalizePageParams(params))
}

export const getMyOrders = (params?: PageParams): Promise<PageResult<OrderItemVo>> => getOrderList({ ...params, role: 'buyer' })
export const getMyTakenOrders = (params?: PageParams): Promise<PageResult<OrderItemVo>> => getOrderList({ ...params, role: 'seller' })

export function getOrderDetail(id: number): Promise<OrderDetailVo> {
  return request.get('/v1/orders/' + id)
}

export function payOrder(id: number): Promise<PayResultVo> {
  return request.post('/v1/orders/' + id + '/pay')
}

export function cancelOrder(id: number, reason: string): Promise<null> {
  return request.post('/v1/orders/' + id + '/cancel', { reason })
}

export function startService(id: number): Promise<null> {
  return request.post('/v1/orders/' + id + '/start')
}

export function getVerifyCode(id: number): Promise<VerifyCodeVo> {
  return request.get('/v1/orders/' + id + '/verify-code')
}

export function refreshVerifyCode(id: number): Promise<VerifyCodeVo> {
  return request.put('/v1/orders/' + id + '/verify-code')
}

/**
 * 核销订单 — 双方核销码驱动
 * @param id 订单ID
 * @param code 核销码
 * @param phase start=开始核销(1→3), complete=完成确认(3→5)
 */
export function verifyOrder(id: number, code: string, phase: 'start' | 'complete'): Promise<null> {
  return request.post('/v1/orders/' + id + '/verify', { code, phase })
}

export function confirmOrder(id: number): Promise<null> {
  return request.post('/v1/orders/' + id + '/confirm')
}

export function disputeOrder(id: number, reason: string, explain?: string): Promise<null> {
  return request.post('/v1/orders/' + id + '/dispute', { reason, explain })
}

export function refundOrder(id: number, reason: string): Promise<null> {
  return request.post('/v1/orders/' + id + '/refund', { reason })
}

export function getOrderReview(orderId: number): Promise<any> {
  return request.get('/v1/orders/' + orderId + '/review')
}
