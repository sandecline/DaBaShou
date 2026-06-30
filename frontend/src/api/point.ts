import request from '@/utils/request'
import type { PointBalanceVo, PointTransVo, PointStatsVo, SignInResultVo, SignInStatusVo, GuaranteePoolVo, PageResult, PageParams } from '@/types/api'
import { normalizePageParams } from './_params'

export function getBalance(): Promise<PointBalanceVo> {
  return request.get('/v1/points/balance')
}

export function getTransactions(params: PageParams & { type?: number; orderId?: number; startDate?: string; endDate?: string }): Promise<PageResult<PointTransVo>> {
  return request.get('/v1/points/transactions', normalizePageParams(params))
}

export function getTransactionDetail(id: number): Promise<PointTransVo> {
  return request.get('/v1/points/transactions/' + id)
}

export function getPointStats(): Promise<PointStatsVo> {
  return request.get('/v1/points/stats')
}

export function signIn(): Promise<SignInResultVo> {
  return request.post('/v1/points/sign-in')
}

export function getSignInStatus(): Promise<SignInStatusVo> {
  return request.get('/v1/points/sign-in/status')
}

export function getGuaranteePool(): Promise<GuaranteePoolVo> {
  return request.get('/v1/points/guarantee-pool')
}
