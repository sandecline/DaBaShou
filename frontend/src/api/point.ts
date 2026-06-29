import request from '@/utils/request'
import type { PointTransaction, PageResult, PageParams } from '@/types'

export function getBalance(): Promise<{ balance: number; frozen: number; available: number }> {
  return request.get('/point/balance')
}

export function getTransactions(params: PageParams): Promise<PageResult<PointTransaction>> {
  return request.get('/point/transactions', params)
}
