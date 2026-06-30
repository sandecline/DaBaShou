import request from '@/utils/request'
import type { ReviewVo, ViolationVo, AppealVo, PageResult, PageParams } from '@/types/api'

export function submitReview(data: { orderId: number; rating: number; content?: string; images?: string[]; isAnonymous?: boolean }): Promise<number> {
  return request.post('/v1/reviews', data)
}

export function getMySentReviews(params?: PageParams): Promise<PageResult<ReviewVo>> {
  return request.get('/v1/reviews/mine', params)
}

export function getMyReceivedReviews(params?: PageParams): Promise<PageResult<ReviewVo>> {
  return request.get('/v1/reviews/received', params)
}

export function reportViolation(data: { targetUserId: number; orderId?: number; type: number; reason: string; evidence?: string[] }): Promise<number> {
  return request.post('/v1/violations', data)
}

export function getMyViolations(params?: PageParams): Promise<PageResult<ViolationVo>> {
  return request.get('/v1/violations/mine', params)
}

export function submitAppeal(data: { violationId: number; reason: string; evidence?: string[] }): Promise<number> {
  return request.post('/v1/appeals', data)
}

export function getMyAppeals(params?: PageParams): Promise<PageResult<AppealVo>> {
  return request.get('/v1/appeals/mine', params)
}