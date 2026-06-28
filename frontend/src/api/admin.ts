import request from '@/utils/request'
import type { ApiResponse, PageResult, UserAdminVo, OrderAdminVo, ViolationAdminVo, AppealAdminVo, CampusAuthVo } from '@/types/api'

export function getUsers(params?: {
  keyword?: string
  status?: number
  pageNum?: number
  pageSize?: number
}): Promise<ApiResponse<PageResult<UserAdminVo>>> {
  return request({ url: '/admin/v1/users', method: 'get', params })
}

export function getUserDetail(id: number): Promise<ApiResponse<UserAdminVo>> {
  return request({ url: `/admin/v1/users/${id}`, method: 'get' })
}

export function toggleUserStatus(id: number, status: number): Promise<ApiResponse<null>> {
  return request({ url: `/admin/v1/users/${id}/status`, method: 'put', data: { status } })
}

export function resetUserPassword(id: number): Promise<ApiResponse<string>> {
  return request({ url: `/admin/v1/users/${id}/reset-password`, method: 'post' })
}

export function getOrders(params?: {
  status?: number
  keyword?: string
  pageNum?: number
  pageSize?: number
}): Promise<ApiResponse<PageResult<OrderAdminVo>>> {
  return request({ url: '/admin/v1/orders', method: 'get', params })
}

export function getOrderDetail(id: number): Promise<ApiResponse<OrderAdminVo>> {
  return request({ url: `/admin/v1/orders/${id}`, method: 'get' })
}

export function arbitrate(id: number, result: string, refundAmount?: number): Promise<ApiResponse<null>> {
  return request({ url: `/admin/v1/orders/${id}/arbitrate`, method: 'post', data: { result, refundAmount } })
}

export function getViolations(params?: {
  status?: number
  pageNum?: number
  pageSize?: number
}): Promise<ApiResponse<PageResult<ViolationAdminVo>>> {
  return request({ url: '/admin/v1/violations', method: 'get', params })
}

export function handleViolation(id: number, result: string): Promise<ApiResponse<null>> {
  return request({ url: `/admin/v1/violations/${id}`, method: 'post', data: { result } })
}

export function getAppeals(params?: {
  status?: number
  pageNum?: number
  pageSize?: number
}): Promise<ApiResponse<PageResult<AppealAdminVo>>> {
  return request({ url: '/admin/v1/appeals', method: 'get', params })
}

export function handleAppeal(id: number, approved: boolean, reason: string): Promise<ApiResponse<null>> {
  return request({ url: `/admin/v1/appeals/${id}`, method: 'post', data: { approved, reason } })
}

export function getCampusAuths(params?: {
  status?: number
  pageNum?: number
  pageSize?: number
}): Promise<ApiResponse<PageResult<CampusAuthVo>>> {
  return request({ url: '/admin/v1/campus-auths', method: 'get', params })
}

export function reviewCampusAuth(id: number, approved: boolean, reason?: string): Promise<ApiResponse<null>> {
  return request({ url: `/admin/v1/campus-auths/${id}`, method: 'post', data: { approved, reason } })
}
