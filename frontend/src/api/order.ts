import request from '@/utils/request'
import type { OrderItemVo, OrderDetailVo, VerifyCodeVo, PayResultVo, PageResult, PageParams } from '@/types/api'
import { normalizePageParams } from './_params'

export function createOrderFromShelf(data: { skillShelfId?: number; shelfId?: number; timeSlotId?: number; remark?: string }): Promise<number> {
  const shelfId = data.shelfId ?? data.skillShelfId
  return request.post('/v1/order/from-shelf', {
    ...data,
    shelfId,
    idempotentToken: crypto.randomUUID(),
  })
}

export function createOrderFromDemand(data: { demandId: number; sellerId?: number; remark?: string }): Promise<number> {
  return request.post('/v1/order/from-demand', {
    ...data,
    idempotentToken: crypto.randomUUID(),
  })
}

export function getOrderList(params: { role?: 'buyer' | 'seller'; status?: number; pageNum?: number; pageSize?: number }): Promise<PageResult<OrderItemVo>> {
  return request.get('/v1/order', normalizePageParams(params))
}

export const getMyOrders = (params?: PageParams): Promise<PageResult<OrderItemVo>> => getOrderList({ ...params, role: 'buyer' })
export const getMyTakenOrders = (params?: PageParams): Promise<PageResult<OrderItemVo>> => getOrderList({ ...params, role: 'seller' })

export function getOrderDetail(id: number): Promise<OrderDetailVo> {
  return request.get('/v1/order/' + id)
}

export function getOrderStatus(id: number): Promise<{ status: number; statusDesc: string }> {
  return request.get('/v1/order/' + id + '/status')
}

export function payOrder(id: number): Promise<PayResultVo> {
  return request.post('/v1/order/' + id + '/pay')
}

export function cancelOrder(id: number, reason: string): Promise<null> {
  return request.post('/v1/order/' + id + '/cancel', { reason })
}

export function startService(id: number): Promise<null> {
  return request.post('/v1/order/' + id + '/start')
}

export function getVerifyCode(id: number): Promise<VerifyCodeVo> {
  return request.get('/v1/order/' + id + '/verify-code')
}

export function refreshVerifyCode(id: number): Promise<VerifyCodeVo> {
  return request.put('/v1/order/' + id + '/verify-code')
}

export function verifyOrder(id: number, verifyCode: string): Promise<null> {
  return request.post('/v1/order/' + id + '/verify', { verifyCode })
}

export function confirmOrder(id: number): Promise<null> {
  return request.post('/v1/order/' + id + '/confirm')
}

export function disputeOrder(id: number, reason: string): Promise<null> {
  return request.post('/v1/order/' + id + '/dispute', { reason })
}

export function arbitrateOrder(id: number, data: { result: string; reason: string; refundAmount?: number }): Promise<null> {
  return request.post('/v1/order/' + id + '/arbitrate', data)
}

export function refundOrder(id: number, reason: string): Promise<null> {
  return request.post('/v1/order/' + id + '/refund', { reason })
}

export function getOrderReview(orderId: number): Promise<any> {
  return request.get('/v1/orders/' + orderId + '/review')
}
