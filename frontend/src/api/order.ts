import request from '@/utils/request'
import type { Order, PageResult, PageParams, VerifyOrderParams } from '@/types'

export function createOrder(data: {
  demandId?: number
  skillShelfId?: number
  timeSlotId?: number
}): Promise<Order> {
  return request.post('/order', data)
}

export function getOrderList(params: PageParams): Promise<PageResult<Order>> {
  return request.get('/order/list', params)
}

export function getOrderDetail(id: number): Promise<Order> {
  return request.get(`/order/${id}`)
}

export function payOrder(id: number): Promise<any> {
  return request.post(`/order/${id}/pay`)
}

export function startService(id: number): Promise<any> {
  return request.post(`/order/${id}/start`)
}

export function verifyOrder(data: VerifyOrderParams): Promise<any> {
  return request.post(`/order/${data.orderId}/verify`, { verifyCode: data.verifyCode })
}

export function confirmComplete(id: number): Promise<any> {
  return request.post(`/order/${id}/confirm`)
}

export function cancelOrder(id: number, reason?: string): Promise<any> {
  return request.post(`/order/${id}/cancel`, { reason })
}

export function getMyOrders(params: PageParams): Promise<PageResult<Order>> {
  return request.get('/order/my', params)
}

export function getMyTakenOrders(params: PageParams): Promise<PageResult<Order>> {
  return request.get('/order/taken', params)
}
