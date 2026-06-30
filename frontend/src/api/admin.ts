import request from '@/utils/request'
import type { UserAdminVo, OrderAdminVo, AdminOverviewVo, DailyTrendItem, UserActiveItem, TrustDistributionItem, CampusAuthAdminVo, ViolationVo, AppealVo, ReviewVo, PageResult, PageParams } from '@/types/api'
import { normalizePageParams } from './_params'

export function getAdminUserList(params: PageParams & { keyword?: string; status?: number }): Promise<PageResult<UserAdminVo>> {
  return request.get('/admin/v1/users', normalizePageParams(params))
}

export function getAdminUserDetail(id: number): Promise<UserAdminVo> {
  return request.get('/admin/v1/users/' + id)
}

export function updateUserStatus(userId: number, status: number): Promise<null> {
  return request.put('/admin/v1/users/' + userId + '/status', { status })
}

export function resetUserPassword(userId: number): Promise<{ newPassword: string }> {
  return request.post('/admin/v1/users/' + userId + '/reset-password')
}

export function getAdminOrderList(params: PageParams & { keyword?: string; status?: number }): Promise<PageResult<OrderAdminVo>> {
  return request.get('/admin/v1/orders', normalizePageParams(params))
}

export function getAdminOrderDetail(id: number): Promise<OrderAdminVo> {
  return request.get('/admin/v1/orders/' + id)
}

export function adminArbitrateOrder(id: number, data: { result: string; reason: string; refundAmount?: number }): Promise<null> {
  return request.post('/admin/v1/orders/' + id + '/arbitrate', data)
}

export function adminHandleViolation(id: number, result: string): Promise<null> {
  return request.post('/admin/v1/violations/' + id, { result })
}

export function adminHandleAppeal(id: number, data: { approved: boolean; reason: string }): Promise<null> {
  return request.post('/admin/v1/appeals/' + id, data)
}

export function getAdminCampusAuths(params: PageParams & { status?: number }): Promise<PageResult<CampusAuthAdminVo>> {
  return request.get('/admin/v1/campus-auths', normalizePageParams(params))
}

export function reviewCampusAuth(id: number, data: { approved: boolean; reason?: string }): Promise<null> {
  return request.post('/admin/v1/campus-auths/' + id, data)
}

export function getAdminOverview(): Promise<AdminOverviewVo> {
  return request.get('/admin/v1/stats/overview')
}

export function getAdminDailyTrend(days?: number): Promise<DailyTrendItem[]> {
  return request.get('/admin/v1/stats/daily-trend', { days })
}

export function getAdminUserActive(days?: number): Promise<UserActiveItem[]> {
  return request.get('/admin/v1/stats/user-active', { days })
}

export function getAdminTrustDistribution(): Promise<TrustDistributionItem[]> {
  return request.get('/admin/v1/stats/trust-distribution')
}

export function exportAdminData(type: string): Promise<Blob> {
  return request.get('/admin/v1/stats/export', { type })
}

export function getAdminViolations(params: PageParams): Promise<PageResult<ViolationVo>> {
  return request.get('/admin/v1/violations', normalizePageParams(params))
}

export function getAdminAppeals(params: PageParams): Promise<PageResult<AppealVo>> {
  return request.get('/admin/v1/appeals', normalizePageParams(params))
}

export function getSystemConfig(): Promise<Record<string, any>> {
  return request.get('/admin/v1/config')
}

export function updateSystemConfig(config: Record<string, any>): Promise<null> {
  return request.put('/admin/v1/config', config)
}

export function getAdminReviews(params: PageParams): Promise<PageResult<ReviewVo>> {
  return request.get('/admin/v1/reviews', normalizePageParams(params))
}

export function deleteReview(reviewId: number): Promise<null> {
  return request.delete('/admin/v1/reviews/' + reviewId)
}
