import { get, post } from '@/utils/request'

export function submitReview(data) { return post('/reviews', data) }

export function getMyReviews(params) { return get('/reviews/mine', params) }

export function getReceivedReviews(params) { return get('/reviews/received', params) }

export function reportViolation(data) { return post('/violations', data) }

export function getMyViolations(params) { return get('/violations/mine', params) }

export function submitAppeal(data) { return post('/appeals', data) }

export function getMyAppeals(params) { return get('/appeals/mine', params) }
