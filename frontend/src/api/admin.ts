import request from '@/utils/request'
import type { User, Order, Review, Violation, Appeal, PageResult, PageParams } from '@/types'

// 用户管理
export function getAdminUserList(params: PageParams): Promise<PageResult<User>> {
  return request.get('/admin/users', params)
}

export function updateUserStatus(userId: number, status: 0 | 1): Promise<any> {
  return request.put(`/admin/users/${userId}/status`, { status })
}

export function resetUserPassword(userId: number): Promise<any> {
  return request.post(`/admin/users/${userId}/reset-password`)
}

// 订单管理
export function getAdminOrderList(params: PageParams): Promise<PageResult<Order>> {
  return request.get('/admin/orders', params)
}

export function handleDisputeOrder(orderId: number, action: 'complete' | 'refund', reason?: string): Promise<any> {
  return request.post(`/admin/orders/${orderId}/dispute`, { action, reason })
}

// 信用管理
export function getAdminReviews(params: PageParams): Promise<PageResult<Review>> {
  return request.get('/admin/reviews', params)
}

export function deleteReview(reviewId: number): Promise<any> {
  return request.delete(`/admin/reviews/${reviewId}`)
}

export function getAdminViolations(params: PageParams): Promise<PageResult<Violation>> {
  return request.get('/admin/violations', params)
}

export function handleViolation(violationId: number, action: 'confirm' | 'dismiss'): Promise<any> {
  return request.post(`/admin/violations/${violationId}/handle`, { action })
}

export function getAdminAppeals(params: PageParams): Promise<PageResult<Appeal>> {
  return request.get('/admin/appeals', params)
}

export function handleAppeal(appealId: number, action: 'approve' | 'reject', reply?: string): Promise<any> {
  return request.post(`/admin/appeals/${appealId}/handle`, { action, reply })
}

// 系统配置
export function getSystemConfig(): Promise<Record<string, any>> {
  return request.get('/admin/config')
}

export function updateSystemConfig(config: Record<string, any>): Promise<any> {
  return request.put('/admin/config', config)
}

// 数据统计
export function getAdminStats(): Promise<any> {
  return request.get('/admin/stats')
}
