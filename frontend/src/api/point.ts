import request from '@/utils/request'
import type { ApiResponse, PageResult, PointBalanceVo, PointTransVo, PointStatsVo, SignInStatusVo, GuaranteePoolVo } from '@/types/api'

export function getBalance(): Promise<ApiResponse<PointBalanceVo>> {
  return request({ url: '/v1/points/balance', method: 'get' })
}

export function getTransactions(params?: {
  type?: number
  orderId?: number
  startDate?: string
  endDate?: string
  pageNum?: number
  pageSize?: number
}): Promise<ApiResponse<PageResult<PointTransVo>>> {
  return request({ url: '/v1/points/transactions', method: 'get', params })
}

export function getTransactionDetail(id: number): Promise<ApiResponse<PointTransVo>> {
  return request({ url: `/v1/points/transactions/${id}`, method: 'get' })
}

export function getPointStats(): Promise<ApiResponse<PointStatsVo>> {
  return request({ url: '/v1/points/stats', method: 'get' })
}

export function signIn(): Promise<ApiResponse<{ reward: number; consecutiveDays: number }>> {
  return request({ url: '/v1/points/sign-in', method: 'post' })
}

export function getSignInStatus(): Promise<ApiResponse<SignInStatusVo>> {
  return request({ url: '/v1/points/sign-in/status', method: 'get' })
}

export function getGuaranteePool(): Promise<ApiResponse<GuaranteePoolVo>> {
  return request({ url: '/v1/points/guarantee-pool', method: 'get' })
}
