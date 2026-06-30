import request from '@/utils/request'
import type { ApiResponse, PageResult, ReviewVo, ReviewDto, ViolationVo, ViolationDto, AppealVo, AppealDto } from '@/types/api'

export function submitReview(data: ReviewDto): Promise<ApiResponse<number>> {
  return request({ url: '/v1/reviews', method: 'post', data })
}

export function getMyReviews(params?: {
  pageNum?: number
  pageSize?: number
}): Promise<ApiResponse<PageResult<ReviewVo>>> {
  return request({ url: '/v1/reviews/mine', method: 'get', params })
}

export function getReceivedReviews(params?: {
  pageNum?: number
  pageSize?: number
}): Promise<ApiResponse<PageResult<ReviewVo>>> {
  return request({ url: '/v1/reviews/received', method: 'get', params })
}

export function getOrderReview(orderId: number): Promise<ApiResponse<ReviewVo>> {
  return request({ url: `/v1/orders/${orderId}/review`, method: 'get' })
}

export function reportViolation(data: ViolationDto): Promise<ApiResponse<number>> {
  return request({ url: '/v1/violations', method: 'post', data })
}

export function getMyViolations(params?: {
  pageNum?: number
  pageSize?: number
}): Promise<ApiResponse<PageResult<ViolationVo>>> {
  return request({ url: '/v1/violations/mine', method: 'get', params })
}

export function submitAppeal(data: AppealDto): Promise<ApiResponse<number>> {
  return request({ url: '/v1/appeals', method: 'post', data })
}

export function getMyAppeals(params?: {
  pageNum?: number
  pageSize?: number
}): Promise<ApiResponse<PageResult<AppealVo>>> {
  return request({ url: '/v1/appeals/mine', method: 'get', params })
}

// Shim: alias for getMyViolations used as getViolations in views
export function getViolations(params?: any): any {
  return getMyViolations(params)
}

// Shim: alias for getMyAppeals used as getAppeals in views
export function getAppeals(params?: any): any {
  return getMyAppeals(params)
}

// Shim: alias for getMyReviews used as getReviews in views
export function getReviews(params?: any): any {
  return getMyReviews(params)
}
