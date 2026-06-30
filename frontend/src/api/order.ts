import request from '@/utils/request'
import type { ApiResponse, PageResult, OrderItemVo, OrderDetailVo, CreateOrderFromShelfDto, CreateOrderFromDemandDto, PayOrderDto } from '@/types/api'
import { generateIdempotentToken } from '@/utils/request'

export function createOrderFromShelf(data: CreateOrderFromShelfDto): Promise<ApiResponse<number>> {
  return request({ url: '/v1/order/from-shelf', method: 'post', data, headers: { 'X-Idempotent-Token': generateIdempotentToken() } })
}

export function createOrderFromDemand(data: CreateOrderFromDemandDto): Promise<ApiResponse<number>> {
  return request({ url: '/v1/order/from-demand', method: 'post', data, headers: { 'X-Idempotent-Token': generateIdempotentToken() } })
}

export function getOrderList(params?: {
  role?: 'buyer' | 'seller'
  status?: number
  pageNum?: number
  pageSize?: number
}): Promise<ApiResponse<PageResult<OrderItemVo>>> {
  return request({ url: '/v1/order', method: 'get', params })
}

export function getOrderDetail(id: number): Promise<ApiResponse<OrderDetailVo>> {
  return request({ url: `/v1/order/${id}`, method: 'get' })
}

export function getOrderStatus(id: number): Promise<ApiResponse<{ status: number; statusName: string }>> {
  return request({ url: `/v1/order/${id}/status`, method: 'get' })
}

export function payOrder(id: number, data?: PayOrderDto): Promise<ApiResponse<null>> {
  return request({ url: `/v1/order/${id}/pay`, method: 'post', data: data || {}, headers: { 'X-Idempotent-Token': generateIdempotentToken() } })
}

export function cancelOrder(id: number, reason: string): Promise<ApiResponse<null>> {
  return request({ url: `/v1/order/${id}/cancel`, method: 'post', data: { reason } })
}

export function startService(id: number): Promise<ApiResponse<null>> {
  return request({ url: `/v1/order/${id}/start`, method: 'post' })
}

export function refreshVerifyCode(id: number): Promise<ApiResponse<string>> {
  return request({ url: `/v1/order/${id}/verify-code`, method: 'put' })
}

export function verifyOrder(id: number, verifyCode: string): Promise<ApiResponse<null>> {
  return request({ url: `/v1/order/${id}/verify`, method: 'post', data: { verifyCode }, headers: { 'X-Idempotent-Token': generateIdempotentToken() } })
}

export function confirmOrder(id: number): Promise<ApiResponse<null>> {
  return request({ url: `/v1/order/${id}/confirm`, method: 'post' })
}

export function disputeOrder(id: number, reason: string): Promise<ApiResponse<null>> {
  return request({ url: `/v1/order/${id}/dispute`, method: 'post', data: { reason } })
}

export function arbitrateOrder(id: number, result: string, reason: string, refundAmount?: number): Promise<ApiResponse<null>> {
  return request({ url: `/v1/order/${id}/arbitrate`, method: 'post', data: { result, reason, refundAmount } })
}

export function refundOrder(id: number, reason: string): Promise<ApiResponse<null>> {
  return request({ url: `/v1/order/${id}/refund`, method: 'post', data: { reason } })
}

export function createOrder(data: any): Promise<ApiResponse<number>> {
  return request({ url: '/v1/orders', method: 'post', data, headers: { 'X-Idempotent-Token': generateIdempotentToken() } })
}

export function getMyOrders(params?: any): Promise<ApiResponse<any>> {
  return request({ url: '/v1/orders', method: 'get', params })
}

export function getMyTakenOrders(params?: any): Promise<ApiResponse<any>> {
  return request({ url: '/v1/orders', method: 'get', params })
}

export function confirmComplete(id: number): Promise<ApiResponse<null>> {
  return request({ url: `/v1/orders/${id}/confirm`, method: 'post' })
}
