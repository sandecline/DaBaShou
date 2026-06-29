import request from '@/utils/request'
import type { Review, Violation, Appeal, ReviewForm, AppealForm, PageResult, PageParams } from '@/types'

export function submitReview(data: ReviewForm): Promise<any> {
  return request.post('/credit/review', data)
}

export function getReviews(params: PageParams & { type?: 'sent' | 'received' }): Promise<PageResult<Review>> {
  return request.get('/credit/reviews', params)
}

export function getViolations(params: PageParams): Promise<PageResult<Violation>> {
  return request.get('/credit/violations', params)
}

export function submitAppeal(data: AppealForm): Promise<any> {
  return request.post('/credit/appeal', data)
}

export function getAppeals(params: PageParams): Promise<PageResult<Appeal>> {
  return request.get('/credit/appeals', params)
}
